package AuctionInterfaces;

import java.rmi.RemoteException;
import java.util.List;

public interface AuctionHouse extends java.rmi.Remote {

  enum Response {ID_NOT_FOUND, RESERVE_NOT_MET, RESERVE_MET}

  int createAuction(String item, String description, float startingPrice, float reservePrice)
      throws RemoteException;

  List<Auction> getListings() throws RemoteException;
}
