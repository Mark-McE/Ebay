package AuctionInterfaces;

import java.io.Serializable;

/**
 * A data structure to store the details of a bidder.
 * Also allows easier creation of Bid objects.
 */
public interface Bidder extends Serializable {

  /**
   * Returns a new Bid object, with this as the bidder, and the specified price
   * as the bid amount
   * @param price The bid amount
   * @return The Bid object, containing this as the bidder, and the specified
   * price as the bid amount.
   */
  Bid createBid(Price price);

  String getName();
  String getEmail();
}
