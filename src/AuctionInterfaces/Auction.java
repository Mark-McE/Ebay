package AuctionInterfaces;

import java.io.Serializable;
import java.util.Optional;

public interface Auction extends Serializable {

  String toReadableString();

  boolean isClosed();
  Optional<Bid> getWinningBid();
  int getId();
  String getItem();
  String getDescription();
  Price getStartingPrice();
  Price getReservePrice();
}
