package AuctionInterfaces;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public interface AuctionHouse extends java.rmi.Remote {

  enum BidResponse {
    OK,
    TOO_LOW,
    AUCTION_NOT_FOUND
  }

  int createAuction(String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException;

  Price createPrice(Float price) throws RemoteException, IllegalArgumentException;

  Optional<Auction> closeAuction(int id) throws RemoteException;

  BidResponse bid(int auctionId, Bid bid) throws RemoteException;

  List<Auction> getLiveAuctions() throws RemoteException;
}
