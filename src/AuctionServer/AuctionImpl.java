package AuctionServer;

import AuctionInterfaces.Auction;

import java.util.concurrent.atomic.AtomicInteger;

public class AuctionImpl implements Auction {
  private static final AtomicInteger idCounter = new AtomicInteger(0);

  private final int id;
  private final String item;
  private final String description;
  private final float startingPrice;
  private final float reservePrice;

  private boolean isClosed;
  private float bestBid;
  private String bestBidName;
  private String bestBidEmail;

  public AuctionImpl(String item, String description, float startingPrice, float reservePrice) {
    if (String.valueOf(startingPrice).split("\\.")[1].length() > 2
    || String.valueOf(reservePrice).split("\\.")[1].length() > 2)
      throw new IllegalArgumentException("prices given have more than 2 decimal places");

    this.id = idCounter.getAndIncrement();
    this.item = item;
    this.description = description;
    this.startingPrice = startingPrice;
    this.reservePrice = reservePrice;

    isClosed = false;
    bestBid = startingPrice;
  }

  @Override
  public String toReadableString() {
    return String.format( "\n\t"
        + item + " (id:" + id + ")" + "\n\t"
        + description + "\n\t"
        + "\n\t"
        + "Starting price: £%.2f \n\t"
        + "Current best bid: £%.2f", startingPrice, getBestBid());
  }

  synchronized boolean bid(String name, String email, float price) {
    if (String.valueOf(price).split("\\.")[1].length() > 2)
      throw new IllegalArgumentException("price given has more than 2 decimal places");
    if (!email.matches("[a-zA-z][\\w\\.-]*@(?:[a-zA-z][\\w\\.-]+\\.)+[a-zA-z]{2,4}"))
      throw new IllegalArgumentException("invalid email address format");
    if (!name.matches("[A-Z][a-zA-z'-]*[a-zA-z] [A-Z][a-zA-z'-]*[a-zA-z]"))
      throw new IllegalArgumentException("invalid name format");

    if (isClosed || price <= bestBid)
      return false;

    bestBidName = name;
    bestBidEmail = email;
    bestBid = price;
    return true;
  }

  synchronized void close() {
    isClosed = true;
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public float getWinnerBid() {
    return isClosed && bestBid > reservePrice ? bestBid : -1;
  }

  @Override
  public String getWinnerName() {
    return isClosed && bestBid > reservePrice  ? bestBidName : null;
  }

  @Override
  public String getWinnerEmail() {
    return isClosed && bestBid > reservePrice  ? bestBidEmail : null;
  }

  synchronized float getBestBid() {
    return bestBid;
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
  public float getStartingPrice() {
    return startingPrice;
  }

  @Override
  public float getReservePrice() {
    return reservePrice;
  }
}
