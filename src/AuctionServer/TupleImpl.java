package AuctionServer;

import AuctionInterfaces.Tuple;

public class TupleImpl<T, S> implements Tuple {

  private final T left;
  private final S right;

  public TupleImpl(T left, S right) {
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
