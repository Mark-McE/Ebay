package AuctionServer;

import AuctionInterfaces.Auction;
import AuctionInterfaces.Bid;
import AuctionInterfaces.Bidder;
import AuctionInterfaces.Price;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the Auction interface
 * @see AuctionInterfaces.Auction
 */
public class AuctionImpl implements Auction, Serializable {

  private final Bidder owner;
  private final int id;
  private final String item;
  private final String description;
  private final Price startingPrice;
  private final Price reservePrice;

  private boolean isClosed;
  private Bid bestBid;

  public AuctionImpl(Bidder owner, int id, String item, String description, Price startingPrice, Price reservePrice) {

    this.owner = owner;
    this.id = id;
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
        + "Current best bid: ", startingPrice.toFloat());

    if (bestBid == null)
      return result + "none";
    return result + String.format("£%.2f", bestBid.toFloat());
  }

  synchronized boolean bid(Bid bid) {
    if (isClosed
        || bestBid == null
        && bid.getPrice().toFloat() <= startingPrice.toFloat()
        || bestBid != null
        && bid.getPrice().toFloat() <= bestBid.getPrice().toFloat())
      return false;

    bestBid = bid;
    return true;
  }

  synchronized AuctionImpl close(Bidder owner) {
    if (this.owner.equals(owner))
      isClosed = true;
    return this;
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public Bid getWinningBid() {
    if (isClosed
        && bestBid != null
        && bestBid.getPrice().toFloat() >= reservePrice.toFloat())
      return bestBid;
    return null;
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

  @Override
  public int hashCode() {
    return Objects.hash(owner, id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (!(obj instanceof AuctionImpl))
      return false;

    AuctionImpl other = (AuctionImpl) obj;
    return other.id == id
        && other.owner.equals(owner);
  }
}
