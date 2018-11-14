package AuctionServer;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse;
import AuctionInterfaces.BidResponse;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AuctionHouseImpl extends java.rmi.server.UnicastRemoteObject
    implements AuctionHouse {

  private Map<Integer, AuctionImpl> auctions = new ConcurrentHashMap<>();

  public AuctionHouseImpl() throws RemoteException {
    super();
  }

  @Override
  public BidResponse bid(int id, float price, String name, String email) throws RemoteException {
    final boolean[] bidOK = new boolean[1];

    Auction auct = auctions.computeIfPresent(id, (auctionId, auction) -> {
      bidOK[0] = auction.bid(name, email, price);
      return auction;
    });

    if (auct == null)
      return BidResponse.AUCTION_NOT_FOUND;
    if (bidOK[0])
      return BidResponse.OK;
    else
      return BidResponse.TOO_LOW;
  }

  @Override
  public Auction closeAuction(int id) throws RemoteException {
    return auctions.computeIfPresent(id, (auctionId, auction) -> auction.close());
  }

  @Override
  public int createAuction(String item, String description, float startingPrice, float reservePrice)
      throws RemoteException {

    AuctionImpl auction = new AuctionImpl(item, description, startingPrice, reservePrice);
    auctions.put(auction.getId(), auction);

    return auction.getId();
  }

  @Override
  public List<Auction> getListings() throws RemoteException {
    return Collections.unmodifiableList(
        auctions.values()
        .stream()
        .filter(auction -> !auction.isClosed())
        .collect(Collectors.toList())
    );
  }
}
