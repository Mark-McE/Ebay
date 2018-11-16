package AuctionServer;

import AuctionInterfaces.Price;
import AuctionInterfaces.PriceFactory;

import java.io.Serializable;

public class PriceFactoryImpl implements PriceFactory, Serializable {
  @Override
  public Price createPrice(float value) {
    return new PriceImpl(value);
  }
}
