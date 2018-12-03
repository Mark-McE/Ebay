package AuctionServer;

import AuctionInterfaces.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Implementation of the AuctionHouse interface
 * @see AuctionInterfaces.AuctionHouse
 */
public class AuctionHouseImpl extends UnicastRemoteObject implements AuctionHouse {

  /** Map of all known auctions (K:auction id, V:auction object) */
  private Map<Integer, AuctionImpl> auctions = new ConcurrentHashMap<>();
  /** Map of clients currently being authenticated (K:client object, V:challenge value) */
  private final Map<Bidder, Integer> authChallenges = new ConcurrentHashMap<>();
  /** List of all known clients */
  private final List<Bidder> clients = new CopyOnWriteArrayList<>();

  private PrivateKey privateKey = (PrivateKey) loadSerializedFile("keys/privateKeyS.ser");
  private Signature dsa;

  public AuctionHouseImpl() throws RemoteException {
    super();

    Signature dsa = null;
    try {
      dsa = Signature.getInstance("SHA1withDSA");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.exit(1);
    }
    this.dsa = dsa;
  }

  @Override
  public Bidder getUser(String email) throws RemoteException {
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
  public ServerResponse bid(int id, Bid bid) throws RemoteException {
    final boolean[] bidOK = new boolean[1];

    Auction auct = auctions.computeIfPresent(id, (auctionId, auction) -> {
      bidOK[0] = auction.bid(bid);
      return auction;
    });

    if (auct == null)
      return ServerResponse.AUCTION_NOT_FOUND;
    if (bidOK[0])
      return ServerResponse.OK;
    else if (auct.isClosed())
      return ServerResponse.AUCTION_CLOSED;
    else
      return ServerResponse.TOO_LOW;
  }

  @Override
  public ServerResponse closeAuction(Bidder owner, int id) throws RemoteException {
    Auction auction = auctions.computeIfPresent(id, (auctionId, auct) -> auct.close(owner));
    if (auction == null)
      return ServerResponse.AUCTION_NOT_FOUND;
    if (auction.isClosed())
      return ServerResponse.OK;
    return ServerResponse.INSUFFICIENT_RIGHTS;
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
  public int createAuction(Bidder owner, String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException {

    AuctionImpl auction = new AuctionImpl(owner, item, description, startingPrice, reservePrice);
    auctions.put(auction.getId(), auction);

    return auction.getId();
  }

  @Override
  public Auction getAuction(int id) throws RemoteException {
    return auctions.get(id);
  }

  @Override
  public List<Auction> getLiveAuctions() throws RemoteException {
    return Collections.unmodifiableList(
        auctions.values()
        .stream()
        .filter(auction -> !auction.isClosed())
        .collect(Collectors.toList()));
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
