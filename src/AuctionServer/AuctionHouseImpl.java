package AuctionServer;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse;
import AuctionServer.DataStructures.AuctionImpl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuctionHouseImpl
    extends java.rmi.server.UnicastRemoteObject
    implements AuctionHouse {

  private List<Auction> auctions = new ArrayList<>();

  public AuctionHouseImpl() throws RemoteException {
    super();
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
