package AuctionServer;

import AuctionInterfaces.Price;
import AuctionInterfaces.PriceFactory;

import java.io.Serializable;

public class PriceFactoryImpl implements PriceFactory, Serializable {
  @Override
  public Price createPrice(int pennies) {
    return new PriceImpl(pennies);
  }
}
