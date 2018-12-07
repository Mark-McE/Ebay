package AuctionInterfaces;

import java.io.Serializable;

/**
 * Object container for three different objects
 * @param <T> class of first object to store
 * @param <S> class of second object to store
 * @param <U> class of third object to store
 */
public interface Triple<T, S, U> extends Serializable {
  T getFirst();
  S getSecond();
  U getThird();
}
