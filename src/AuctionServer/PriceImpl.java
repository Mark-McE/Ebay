package AuctionServer;

import AuctionInterfaces.Price;

import java.io.Serializable;

public class PriceImpl implements Price, Serializable {
  private final int pennies;

  /**
   * @param pennies
   */
  public PriceImpl(int pennies) {
    if (pennies < 0)
      throw new IllegalArgumentException("price cannot be negative");
    this.pennies = pennies;
  }

  @Override
  public float toFloat() {
    return pennies / 100f;
  }
}
