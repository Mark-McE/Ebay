package AuctionInterfaces;

import java.rmi.RemoteException;
import java.util.List;

public interface AuctionHouse extends java.rmi.Remote {

  int createAuction(String item, String description, float startingPrice, float reservePrice)
      throws RemoteException;

  Auction closeAuction(int id) throws RemoteException;

  BidResponse bid(int id, float price, String name, String email) throws RemoteException;

  List<Auction> getListings() throws RemoteException;
}
