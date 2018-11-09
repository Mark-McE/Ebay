package AuctionServer.DataStructures;

import AuctionInterfaces.Auction;

import java.util.concurrent.atomic.AtomicInteger;

public class AuctionImpl implements Auction {
  private static final AtomicInteger idCounter = new AtomicInteger(0);

  private final int id;
  private final String item;
  private final String description;
  private final float startingPrice;
  private final float reservePrice;
  private float bestBid;

  public AuctionImpl(String item, String description, float startingPrice, float reservePrice) {
    if (String.valueOf(startingPrice).split("\\.")[1].length() > 2
    || String.valueOf(reservePrice).split("\\.")[1].length() > 2)
      throw new IllegalArgumentException("prices given have more than 2 decimal places");

    this.id = idCounter.getAndIncrement();
    this.item = item;
    this.description = description;
    this.startingPrice = startingPrice;
    this.reservePrice = reservePrice;

    bestBid = startingPrice;
  }

  @Override
  public String toReadableString() {
    return "\n\t"
        + item + " (id:" + id + ")" + "\n\t"
        + description + "\n\t"
        + "\n\t"
        + "Starting price: £" + startingPrice;
  }

  synchronized boolean bid(float price) {
    if (String.valueOf(price).split("\\.")[1].length() > 2)
      throw new IllegalArgumentException("price given has more than 2 decimal places");

    if (price <= bestBid)
      return false;

    bestBid = price;
    return true;
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
