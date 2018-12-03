package AuctionInterfaces;

import java.io.Serializable;

/**
 * Object container for two different objects
 * @param <T> class of first object to store
 * @param <S> class of second object to store
 */
public interface Pair<T, S> extends Serializable {
  T getLeft();
  S getRight();
}
