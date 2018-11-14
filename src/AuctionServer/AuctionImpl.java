package AuctionServer;

import AuctionInterfaces.Auction;
import AuctionInterfaces.Bid;
import AuctionInterfaces.Price;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionImpl implements Auction {
  private static final AtomicInteger idCounter = new AtomicInteger(0);
  // TODO solve problem of incrementing to int.maxValue

  private final int id;
  private final String item;
  private final String description;
  private final Price startingPrice;
  private final Price reservePrice;

  private boolean isClosed;
  private Bid bestBid;

  public AuctionImpl(String item, String description, Price startingPrice, Price reservePrice) {

    this.id = idCounter.getAndIncrement();
    this.item = item;
    this.description = description;
    this.startingPrice = startingPrice;
    this.reservePrice = reservePrice;

    isClosed = false;
    bestBid = null;
  }

  @Override
  public String toReadableString() {
    String result = String.format( "\n\t"
        + item + " (id:" + id + ")" + "\n\t"
        + description + "\n\t"
        + "\n\t"
        + "Starting price: £%.2f \n\t"
        + "Current best bid: ", startingPrice);

    if (bestBid == null)
      return result + "none";
    return result + String.format("£%.2f", bestBid.getPrice().toFloat());
  }

  synchronized boolean bid(Bid bid) {
    if (isClosed
        || bestBid != null
        && bid.getPrice().toFloat() <= bestBid.getPrice().toFloat())
      return false;

    bestBid = bid;
    return true;
  }

  synchronized AuctionImpl close() {
    isClosed = true;
    return this;
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public Optional<Bid> getWinningBid() {
    if (isClosed
        && bestBid != null
        && bestBid.getPrice().toFloat() > reservePrice.toFloat())
      return Optional.of(bestBid);
    return Optional.empty();
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getItem() {
    return item;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Price getStartingPrice() {
    return startingPrice;
  }

  @Override
  public Price getReservePrice() {
    return reservePrice;
  }
}
