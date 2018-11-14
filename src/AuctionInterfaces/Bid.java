package AuctionInterfaces;

import java.io.Serializable;

public interface Bid extends Serializable {

  Bidder getBidder();
  Price getPrice();
}
