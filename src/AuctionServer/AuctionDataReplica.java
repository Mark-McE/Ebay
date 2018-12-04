package AuctionServer;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse.ServerResponse;
import AuctionInterfaces.Bid;
import AuctionInterfaces.Bidder;
import AuctionInterfaces.Price;
import org.jgroups.*;
import org.jgroups.fork.ForkChannel;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class AuctionDataReplica extends ReceiverAdapter {
  private static final String channelName = "MarkMcElroyAuctionDataCluster05921";

  JChannel channel;
  View view;

  /** Map of all known auctions (K:auction id, V:auction object) */
  private final ConcurrentMap<Integer, AuctionImpl> auctions = new ConcurrentHashMap<>();

  public AuctionDataReplica() {
    super();
  }

  private void start() throws Exception {
    channel = new JChannel();
    channel.setReceiver(this);
    channel.connect(channelName);
    channel.getState(null, 3000);
  }

  @Override
  public void receive(Message msg) {
    System.out.println("message recieved: " + msg);
  }

  @Override
  public void viewAccepted(View view) {
    List<Address> left_members = Util.leftMembers(this.view, view);
    this.view=view;

//    Address localAddress = channel.getLocalAddress();
    int clusterSize = view.size();
    List<Address> members = view.getMembers();

    System.out.println("** view: " + view);
  }

  @Override
  public void getState(OutputStream output) throws Exception {
    synchronized (auctions) {
      Util.objectToStream(auctions, new DataOutputStream(output));
    }
  }

  @Override
  public void setState(InputStream input) throws Exception {
    ConcurrentMap<Integer, AuctionImpl> auctions
        = (ConcurrentMap<Integer, AuctionImpl>) Util.objectFromStream(new DataInputStream(input));

    synchronized (this.auctions) {
      this.auctions.clear();
      this.auctions.putAll(auctions);
    }
  }

  public ServerResponse bid(int id, Bid bid) {
    final boolean[] bidOK = new boolean[1];

    Auction auct = auctions.computeIfPresent(id, (auctionId, auction) -> {
      bidOK[0] = auction.bid(bid);
      return auction;
    });

    if (auct == null)
      return ServerResponse.AUCTION_NOT_FOUND;
    if (bidOK[0])
      return ServerResponse.OK;
    else if (auct.isClosed())
      return ServerResponse.AUCTION_CLOSED;
    else
      return ServerResponse.TOO_LOW;
  }

  public ServerResponse closeAuction(Bidder owner, int id) {
    Auction auction = auctions.computeIfPresent(id, (auctionId, auct) -> auct.close(owner));
    if (auction == null)
      return ServerResponse.AUCTION_NOT_FOUND;
    if (auction.isClosed())
      return ServerResponse.OK;
    return ServerResponse.INSUFFICIENT_RIGHTS;
  }

  public int createAuction(Bidder owner, String item, String description, Price startingPrice, Price reservePrice)
      throws RemoteException {

    AuctionImpl auction = new AuctionImpl(owner, item, description, startingPrice, reservePrice);
    auctions.put(auction.getId(), auction);

    return auction.getId();
  }

  public Auction getAuction(int id) {
    return auctions.get(id);
  }

  public List<Auction> getLiveAuctions() {
    return Collections.unmodifiableList(
        auctions.values()
            .stream()
            .filter(auction -> !auction.isClosed())
            .collect(Collectors.toList()));
  }

  public static void main(String[] args) throws Exception {
    new AuctionDataReplica().start();
  }
}
