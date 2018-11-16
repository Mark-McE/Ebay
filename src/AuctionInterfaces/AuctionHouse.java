package AuctionInterfaces;

import java.rmi.RemoteException;
import java.util.List;

public interface AuctionHouse extends java.rmi.Remote {
  enum BidResponse {
    OK,
    TOO_LOW,
    AUCTION_CLOSED,
    AUCTION_NOT_FOUND
  }

  Bidder createBidder(String name, String email) throws RemoteException;

  int createAuction(String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException;

  Price createPrice(Float price) throws RemoteException, IllegalArgumentException;

  Auction closeAuction(int id) throws RemoteException;

  BidResponse bid(int auctionId, Bid bid) throws RemoteException;

  List<Auction> getLiveAuctions() throws RemoteException;
}
