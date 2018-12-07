package AuctionServer;

import AuctionInterfaces.Triple;

import java.io.Serializable;

/**
 * Implementation of the Triple interface
 * @see AuctionInterfaces.Triple
 */
public class TripleImpl<T, S, U> implements Serializable, Triple {

  private final T first;
  private final S second;
  private final U third;

  public TripleImpl(T first, S second, U third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public T getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  public U getThird() {
    return third;
  }
}
