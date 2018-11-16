package AuctionInterfaces;

import java.io.Serializable;

/**
 * Allows easy creation of new Price objects
 */
public interface PriceFactory extends Serializable{
  Price createPrice(float value);
}
