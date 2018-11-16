package AuctionInterfaces;

import java.io.Serializable;

/**
 * A data structure containing a legal price which can be used as a bid on the
 * auction house server
 */
public interface Price extends Serializable {

  float toFloat();
}
