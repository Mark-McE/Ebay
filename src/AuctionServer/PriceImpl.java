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
    this.price = Math.round(price * 100) / 100f;
  }

  @Override
  public float toFloat() {
    return price;
  }
}
