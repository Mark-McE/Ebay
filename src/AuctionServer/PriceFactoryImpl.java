package AuctionServer;

import AuctionInterfaces.Price;
import AuctionInterfaces.PriceFactory;

import java.io.Serializable;

/**
 * Implementation of the PriceFactory interface
 * @see AuctionInterfaces.PriceFactory
 */
public class PriceFactoryImpl implements PriceFactory, Serializable {
  @Override
  public Price createPrice(int pennies) {
    return new PriceImpl(pennies);
  }
}
