package AuctionServer;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse;
import AuctionInterfaces.Bid;
import AuctionInterfaces.Price;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AuctionHouseImpl extends UnicastRemoteObject implements AuctionHouse {

  private Map<Integer, AuctionImpl> auctions = new ConcurrentHashMap<>();

  public AuctionHouseImpl() throws RemoteException {
    super();
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
    else
      return BidResponse.TOO_LOW;
  }

  @Override
  public Optional<Auction> closeAuction(int id) throws RemoteException {
    return Optional.of(auctions.computeIfPresent(id, (auctionId, auction) -> auction.close()));
  }

  @Override
  public int createAuction(String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException {

    AuctionImpl auction = new AuctionImpl(item, description, startingPrice, reservePrice);
    auctions.put(auction.getId(), auction);

    return auction.getId();
  }

  @Override
  public Price createPrice(Float price) throws RemoteException, IllegalArgumentException {
    if (price < 0)
      throw new IllegalArgumentException("negative values not accepted");
    return new PriceImpl(price);
  }

  @Override
  public List<Auction> getLiveAuctions() throws RemoteException {
    return Collections.unmodifiableList(
        auctions.values()
        .stream()
        .filter(auction -> !auction.isClosed())
        .collect(Collectors.toList()));
  }
}
