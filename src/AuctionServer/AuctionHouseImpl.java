package AuctionServer;

import AuctionInterfaces.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the AuctionHouse interface
 * @see AuctionInterfaces.AuctionHouse
 */
public class AuctionHouseImpl extends UnicastRemoteObject implements AuctionHouse {

  /**
   * Map of (K:auction id, V:auction object)
   */
  private Map<Integer, AuctionImpl> auctions = new ConcurrentHashMap<>();

  private PrivateKey privateKey = (PrivateKey) loadSerializedFile("../../keys/privateKeyS");
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
  public BidResponse bid(int id, Bid bid) throws RemoteException {
    final boolean[] bidOK = new boolean[1];

    Auction auct = auctions.computeIfPresent(id, (auctionId, auction) -> {
      bidOK[0] = auction.bid(bid);
      return auction;
    });

    if (auct == null)
      return BidResponse.AUCTION_NOT_FOUND;
    if (bidOK[0])
      return BidResponse.OK;
    else if (auct.isClosed())
      return BidResponse.AUCTION_CLOSED;
    else
      return BidResponse.TOO_LOW;
  }

  @Override
  public Auction closeAuction(int id) throws RemoteException {
    return auctions.computeIfPresent(id, (auctionId, auction) -> auction.close());
  }

  @Override
  public Bidder createBidder(String name, String email) throws RemoteException {
    return new BidderImpl(name, email);
  }

  @Override
  public int createAuction(String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException {

    AuctionImpl auction = new AuctionImpl(item, description, startingPrice, reservePrice);
    auctions.put(auction.getId(), auction);

    return auction.getId();
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
        FileInputStream fileIn = new FileInputStream(fileName + ".ser");
        ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
      result = objIn.readObject();
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return result;
  }
}
