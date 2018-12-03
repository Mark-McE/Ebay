package AuctionServer;

import AuctionInterfaces.Pair;

import java.io.Serializable;

/**
 * Implementation of the Pair interface
 * @see AuctionInterfaces.Pair
 */
public class PairImpl<T, S> implements Pair, Serializable {

  private final T left;
  private final S right;

  public PairImpl(T left, S right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public T getLeft() {
    return left;
  }

  @Override
  public S getRight() {
    return right;
  }
}
