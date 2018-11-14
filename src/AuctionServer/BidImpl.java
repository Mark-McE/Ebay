package AuctionServer;

import AuctionInterfaces.Bid;
import AuctionInterfaces.Bidder;
import AuctionInterfaces.Price;

import java.io.Serializable;

public class BidImpl implements Bid, Serializable{

  private final Bidder bidder;
  private final Price price;

  BidImpl(Bidder bidder, Price price) {
    this.bidder = bidder;
    this.price = price;
  }

  @Override
  public Bidder getBidder() {
    return bidder;
  }

  @Override
  public Price getPrice() {
    return price;
  }
}
