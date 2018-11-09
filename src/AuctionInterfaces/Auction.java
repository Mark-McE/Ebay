package AuctionInterfaces;

import java.io.Serializable;

public interface Auction extends Serializable {

  String toReadableString();

  int getId();

  String getItem();

  String getDescription();

  float getStartingPrice();

  float getReservePrice();
}
