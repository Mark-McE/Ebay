package AuctionServer;

import AuctionInterfaces.Bid;
import AuctionInterfaces.Bidder;
import AuctionInterfaces.Price;

import java.io.Serializable;

public class BidderImpl implements Bidder, Serializable {
  private final String name;
  private final String email;

  public BidderImpl(String name, String email) {
    if (!email.matches("[a-zA-z][\\w\\.-]*@(?:[a-zA-z][\\w\\.-]+\\.)+[a-zA-z]{2,4}"))
      throw new IllegalArgumentException("invalid email address format");
    if (!name.matches("[A-Za-z][a-zA-z'-]*[a-zA-z] [A-Za-z][a-zA-z'-]*[a-zA-z]"))
      throw new IllegalArgumentException("invalid name format");

    this.name = name;
    this.email = email;
  }

  @Override
  public Bid createBid(Price price) {
    return new BidImpl(this, price);
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getEmail() {
    return null;
  }
}
