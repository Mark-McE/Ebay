package AuctionInterfaces;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.List;

/**
 * An auctioning server that deals with requests from clients, and maintains the
 * state of the ongoing auctions.
 *
 * Supports multiple seller and buyer clients to create or bid simultaneously.
 */
public interface AuctionHouse extends java.rmi.Remote {

  /**
   * All possible responses to placing a bid on the auction house
   */
  enum BidResponse {
    OK,
    TOO_LOW,
    AUCTION_CLOSED,
    AUCTION_NOT_FOUND
  }

  /**
   * Sends the first message of the authentication protocol, and returns the
   * second message (response).
   * Authentication is required before further interaction with the Auction House
   * may take place.
   * @param sender
   * @param challenge
   * @return
   * @throws IllegalStateException if bidder has already sent the first message
   *    of the authentication protocol, and not responded to the reply.
   */
  Pair<byte[], Integer> beginAuth(Bidder sender, int challenge)
      throws RemoteException, IllegalStateException;

  /**
   * Sends the third and final message in the authentication protocol, and returns
   * true if authentication was successful.
   * @param sender
   * @param response
   * @return true if authentication was successful, false if response was incorrect.
   * @throws IllegalStateException
   */
  boolean finalizeAuth(Bidder sender, byte[] response)
      throws RemoteException, IllegalStateException;

  /**
   * Creates a Bidder object with the specified name and email and returns it
   * @param name The bidder's name
   * @param email The bidder's email address
   * @param publicKey The bidder's public key used for DSA
   * @return The Bidder object containing the passed name and email
   */
  Bidder createBidder(String name, String email, PublicKey publicKey) throws RemoteException;

  /**
   * Creates a new auction and lists it on the server
   * @param item The name of the item fro sale
   * @param description A short description of the item for sale
   * @param startingPrice The initial bid on the item for sale
   * @param reservePrice The minimum price to sell the item at.
   * @return The auction id for the newly created auction.
   */
  int createAuction(String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException;

  /**
   * Attempts the close the auction defined by the auction id passed.
   * Returns the auction if the auctions was closed, or null if the auction was
   * not found.
   * @param id The auction id of the auction to close
   * @return The auction which is now closed. or null if the auction could not be found
   */
  Auction closeAuction(int id) throws RemoteException;

  /**
   * Attempts to make a new bid on auctions specified by the passed auction id
   * @param auctionId The auction id of the auction to bid on
   * @param bid The bid to make on the auction
   * @return A response which states the result of the bid.
   */
  BidResponse bid(int auctionId, Bid bid) throws RemoteException;

  /**
   * Returns a list of all current known, open auctions on the auction house server
   * @return A list of all current known, open auctions on the auction house server
   */
  List<Auction> getLiveAuctions() throws RemoteException;

  /**
   * Returns a Factory object to create new Price objects easily.
   * @return A Factory object to create new Price objects easily.
   */
  PriceFactory getPriceFactory() throws RemoteException;
}
