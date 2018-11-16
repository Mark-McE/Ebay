package AuctionInterfaces;

import java.io.Serializable;

public interface Bidder extends Serializable {

  Bid createBid(Price price);

  String getName();
  String getEmail();
}
