package AuctionInterfaces;

import java.io.Serializable;

public interface Auction extends Serializable {

  default String toReadableString() {
    String result = String.format( "\n\t"
        + getItem() + " (id:" + getId() + ")" + "\n\t"
        + getDescription() + "\n\t"
        + "\n\t"
        + "Starting price: £%.2f \n\t"
        + "Current best bid: ", getStartingPrice().toFloat());

    if (getWinningBid() == null)
      return result + "none";
    return result + String.format("£%.2f", getWinningBid().toFloat());
  }

  boolean isClosed();
  Bid getWinningBid();
  int getId();
  String getItem();
  String getDescription();
  Price getStartingPrice();
  Price getReservePrice();
}
