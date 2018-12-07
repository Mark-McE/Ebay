package AuctionServer;

import AuctionInterfaces.*;
import org.jgroups.JChannel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static AuctionServer.ClusterMember.Service.*;

/**
 * Implementation of the AuctionHouse interface
 * @see AuctionInterfaces.AuctionHouse
 */
public class AuctionHouseImpl extends UnicastRemoteObject implements AuctionHouse {

  private static final int TIMEOUT = 2000;

  /** Map of clients currently being authenticated (K:client object, V:challenge value) */
  private final ConcurrentMap<Bidder, Integer> authChallenges = new ConcurrentHashMap<>();
  /** List of all known clients */
  private final List<Bidder> clients = new CopyOnWriteArrayList<>();

  private ClusterRequester clusterRequester;
  private final PrivateKey privateKey = (PrivateKey) loadSerializedFile("keys/privateKeyS.ser");
  private Signature dsa;

  public AuctionHouseImpl() throws RemoteException {
    super();

    try {
      dsa = Signature.getInstance("SHA1withDSA");
    } catch (NoSuchAlgorithmException e) {
      // would only execute if SHA1withDSA is removed as a Signature algorithm
      e.printStackTrace();
      System.exit(1);
    }

    try {
      clusterRequester = new ClusterRequester(new JChannel("props.xml"));
    } catch (Exception e) {
      System.out.println("Error: Unable to connect to cluster");
      e.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  public Bidder getBidder(String email) throws RemoteException {
    return clients.stream()
        .filter(b -> b.getEmail().equals(email))
        .findFirst()
        .orElse(null);
  }

  @Override
  public synchronized Pair<byte[], Integer> beginAuth(Bidder sender, int challenge)
      throws RemoteException, IllegalStateException {
    if (authChallenges.containsKey(sender))
        throw new IllegalStateException("current authentication session not finalized");

    byte[] response = null;
    try {
      dsa.initSign(privateKey);
      dsa.update(BigInteger.valueOf(challenge).toByteArray());
      response = dsa.sign();
    } catch (InvalidKeyException | SignatureException e) {
      // should never execute
      e.printStackTrace();
      System.exit(1);
    }

    int myChallenge = new SecureRandom().nextInt();
    authChallenges.put(sender, myChallenge);
    return new PairImpl<byte[], Integer>(response, myChallenge);
  }

  @Override
  public synchronized boolean finalizeAuth(Bidder sender, byte[] response)
      throws RemoteException, IllegalStateException {
    if (!authChallenges.containsKey(sender))
      throw new IllegalStateException("No current authentication session to finalize.");

    boolean res = false;
    try {
      dsa.initVerify(sender.getPublicKey());
      dsa.update(BigInteger.valueOf(authChallenges.get(sender)).toByteArray());
      res = dsa.verify(response);
    } catch (InvalidKeyException | SignatureException e) {
      // should never execute
      e.printStackTrace();
      System.exit(1);
    }

    authChallenges.remove(sender);
    return res;
  }

  @Override
  public Bidder createBidder(String name, String email, PublicKey publicKey) throws RemoteException {
    if (clients.stream().anyMatch(b -> b.getEmail().equals(email)))
      return null;
    Bidder bidder = new BidderImpl(name, email, publicKey);
    clients.add(bidder);
    return bidder;
  }

  @Override
  public ServerResponse bid(int id, Bid bid) throws RemoteException {
    try {
      return (ServerResponse) clusterRequester.requestService(BID, TIMEOUT, id, bid);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  @Override
  public ServerResponse closeAuction(Bidder owner, int id) throws RemoteException {
    try {
      return (ServerResponse) clusterRequester.requestService(CLOSE_AUCTION, TIMEOUT, owner, id);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  @Override
  public int createAuction(Bidder owner, String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException {
    try {
      return (int) clusterRequester.requestService(CREATE_AUCTION, TIMEOUT, owner, item, description, startingPrice, reservePrice);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return -1;
  }

  @Override
  public Auction getAuction(int id) throws RemoteException {
    try {
      return (Auction) clusterRequester.requestService(GET_AUCTION, TIMEOUT, id);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  @Override
  public List<Auction> getLiveAuctions() throws RemoteException {
    try {
      return (List<Auction>) clusterRequester.requestService(GET_LIVE_AUCTIONS, TIMEOUT);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  @Override
  public PriceFactory getPriceFactory() {
    return new PriceFactoryImpl();
  }

  /**
   * loads the java object stored in the serialized file passed
   * @param fileName The name of the file to read
   * @return The java object stored in the file
   */
  private Object loadSerializedFile(String fileName) {
    Object result = null;
    try (
        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
      result = objIn.readObject();
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return result;
  }
}
