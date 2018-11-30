package AuctionServer;

import AuctionInterfaces.Bid;
import AuctionInterfaces.Bidder;
import AuctionInterfaces.Price;

import java.io.Serializable;
import java.security.PublicKey;

public class BidderImpl implements Bidder, Serializable {
  private final String name;
  private final String email;
  private final PublicKey publicKey;

  public BidderImpl(String name, String email, PublicKey publicKey) {
    if (!email.matches("[a-zA-z][\\w\\.-]*@(?:[a-zA-z][\\w\\.-]+\\.)+[a-zA-z]{2,4}"))
      throw new IllegalArgumentException("invalid email address format");
    if (!name.matches("[A-Za-z][a-zA-z'-]*[a-zA-z] [A-Za-z][a-zA-z'-]*[a-zA-z]"))
      throw new IllegalArgumentException("invalid name format");

    this.publicKey = publicKey;
    this.name = name;
    this.email = email;
  }

  @Override
  public Bid createBid(Price price) {
    return new BidImpl(this, price);
  }

  @Override
  public PublicKey getPublicKey() {
    return publicKey;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEmail() {
    return email;
  }
}
