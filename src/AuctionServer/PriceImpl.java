package AuctionServer;

import AuctionInterfaces.Price;

import java.io.Serializable;

public class PriceImpl implements Price, Serializable {
  private final float price;

  /**
   * Rounds price to two decimal places
   * @param price
   */
  public PriceImpl(float price) {
    if (price >= Integer.MAX_VALUE/100)
      throw new IllegalArgumentException("price cannot exceed Integer.MAX_VALUE/100");
    this.price = Math.round(price * 100) / 100f;
  }

  @Override
  public float toFloat() {
    return price;
  }
}
