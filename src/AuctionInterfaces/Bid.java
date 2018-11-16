package AuctionInterfaces;

import java.io.Serializable;

/**
 * A data structure to hold the price of a bid, and the details of the
 * bidder making the bid.
 */
public interface Bid extends Serializable {

  Bidder getBidder();
  Price getPrice();

  default float toFloat() {
    return getPrice().toFloat();
  }

  default String getBidderName() {
    return getBidder().getName();
  }

  default String getBidderEmail() {
    return getBidder().getEmail();
  }
}
