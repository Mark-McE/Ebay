package AuctionInterfaces;

import java.io.Serializable;

/**
 * A Data structure to hold all information about a single auction
 */
public interface Auction extends Serializable {

  /**
   * Returns a human readable description of the auction.
   * Should include
   *   auction id
   *   item
   *   item description
   *   stating price
   *   winning bid (if any)
   * @return A human readable description of this auction.
   */
  String toReadableString();

  /**
   * Determines if this auction is closed.
   * Once closed, an auction can never open again.
   * Bids cannot be placed on closed auctions.
   * @return true if auction is closed. false otherwise
   */
  boolean isClosed();

  /**
   * Returns the winning bid if any.
   * Always returns null if auction is not closed.
   * @return The winning Bid if any. Or null if best bid did not exceed the
   * reserve price, or null if the auction is not closed yet.
   */
  Bid getWinningBid();

  /**
   * returns the auction id
   * @return the auction id
   */
  int getId();

  /**
   * returns the name of the item for sale in this auction
   * @return the name of the item for sale
   */
  String getItem();

  /**
   * returns a short description of the item for sale in this auction
   * @return A short description of the item for sale
   */
  String getDescription();

  /**
   * returns the initial price this auctions began at
   * @return The initial price this auctions began at
   */
  Price getStartingPrice();

  /**
   * returns the secret reserve price for this auction. If the best bid at the
   * time the auction closes does not exeed the reserve price. The item is not sold
   *
   * @return the secret reserve price for this auction
   */
  Price getReservePrice();
}
