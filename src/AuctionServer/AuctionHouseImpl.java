package AuctionServer;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuctionHouseImpl
    extends java.rmi.server.UnicastRemoteObject
    implements AuctionHouse {

  private List<Auction> auctions = new ArrayList<>();

  public AuctionHouseImpl() throws RemoteException {
    super();
  }

  @Override
  synchronized public Auction closeAuction(int id) throws RemoteException {
    final Auction[] result = new Auction[1];

    auctions.stream()
        .filter((Auction a) -> a.getId() == id)
        .findFirst()
        .ifPresent(auction -> {
          auctions.remove(auction);
          ((AuctionImpl) auction).close();
          result[0] = auction;
        });

    return result[0];
  }

  @Override
  public int createAuction(String item, String description,
      float startingPrice, float reservePrice)
      throws RemoteException {

    Auction auction = new AuctionImpl(item, description, startingPrice, reservePrice);
    auctions.add(auction);

    return auction.getId();
  }

  @Override
  public List<Auction> getListings() throws RemoteException {
    return Collections.unmodifiableList(auctions);
  }
}
