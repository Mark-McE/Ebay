package AuctionInterfaces;

import java.io.Serializable;

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
