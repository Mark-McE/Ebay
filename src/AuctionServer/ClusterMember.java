package AuctionServer;

import AuctionInterfaces.*;
import AuctionInterfaces.AuctionHouse.ServerResponse;
import org.jgroups.*;
import org.jgroups.blocks.ReplicatedHashMap;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ClusterMember extends ReceiverAdapter {

  /**
   * All services which may be requested from this cluster member
   */
  public enum Service {
    BID(Integer.class, Bid.class),
    CREATE_AUCTION(Bidder.class, String.class, String.class, Price.class, Price.class),
    CLOSE_AUCTION(Bidder.class, Integer.class),
    GET_AUCTION(Integer.class),
    GET_LIVE_AUCTIONS(),
    RECOVER_STATE();

    /**
     * the Class objects representing the classes for this service's required parameters
     */
    final Class[] params;
    Service(Class... params) {
      this.params = params;
    }
  }

  // JGroups variables
  static final String channelName = "MarkMcElroyAuctionData05921";
  private Channel channel;

  // state variables (see Receiver.getState)
  /** Map of all known auctions (K:auction id, V:auction object) */
  private ReplicatedHashMap<Integer, AuctionImpl> auctions;
  /** Counter for new auction id numbers */
  private final AtomicInteger idCounter = new AtomicInteger(0);

  private void start() throws Exception {
    channel = new JChannel("props.xml");
    channel.setReceiver(this);
    channel.connect(channelName);

    // ReplicatedHashMap overrides the local implementation of Receiver interface
    // so we override the map's implemented methods to include our own implementation
    this.auctions = new ReplicatedHashMap<Integer, AuctionImpl>(channel) {
      @Override
      public void receive(Message msg) {
        ClusterMember.this.receive(msg);
      }

      @Override
      public void getState(OutputStream ostream) throws Exception {
        super.getState(ostream);
        ClusterMember.this.getState(ostream);
      }

      @Override
      public void setState(InputStream istream) throws Exception {
        super.setState(istream);
        ClusterMember.this.setState(istream);
      }
    };
    channel.getState(null, 1000);
    System.out.println("[CM] init complete");
  }

  @Override
  public void receive(Message msg) {
    System.out.println("[CM] msg received: " + msg);

    // if this is coord, broadcast this message to all nodes
    // this ensures total ordering
    if (channel.getAddress().equals(channel.getView().getCoord())) {
      // only coord broadcasts messages (dest == null)
      // so here we ignore the message we sent to our self
      if (msg.dest() == null)
        return;

      try {
        System.out.println("[CM-coord] sending to all (dest: null) " + msg);
        channel.send(new Message(null, msg.getBuffer()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    Address returnAddress;
    Service service;
    Object[] params;
    try {
      if (!(Util.objectFromByteBuffer(msg.getBuffer()) instanceof Triple))
        return;
      Triple<Service, Object[], Address> triple = (Triple) Util.objectFromByteBuffer(msg.getBuffer());
      service = triple.getFirst();
      params = triple.getSecond();
      returnAddress = triple.getThird();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    Object returnObj = null;
    switch (service) {
      case BID:
        returnObj = bid((int)params[0], (Bid)params[1]);
        break;
      case CREATE_AUCTION:
        returnObj = createAuction((Bidder)params[0], (String)params[1],
            (String)params[2], (Price)params[3], (Price)params[4]);
        break;
      case CLOSE_AUCTION:
        returnObj = closeAuction((Bidder)params[0], (int)params[1]);
        break;
      case GET_AUCTION:
        returnObj = getAuction((int)params[0]);
        break;
      case GET_LIVE_AUCTIONS:
        returnObj = getLiveAuctions();
        break;
      case RECOVER_STATE:
        try {
          channel.getState(null, 2000);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
    }

    try {
      Message returnMsg = new Message(returnAddress, Util.objectToByteBuffer(returnObj));
      System.out.println("[CM] sending to CR " + returnMsg);
      channel.send(returnMsg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void getState(OutputStream output) throws Exception {
    synchronized(idCounter) {
      Util.objectToStream(idCounter, new DataOutputStream(output));
    }
  }

  @Override
  public void setState(InputStream input) throws Exception {
    AtomicInteger atomicInteger = (AtomicInteger)Util.objectFromStream(new DataInputStream(input));
    synchronized(idCounter) {
      idCounter.set(atomicInteger.get());
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

  private ServerResponse closeAuction(Bidder owner, int id) {
    Auction auction = auctions.computeIfPresent(id, (auctionId, auct) -> auct.close(owner));
    if (auction == null)
      return ServerResponse.AUCTION_NOT_FOUND;
    if (auction.isClosed())
      return ServerResponse.OK;
    return ServerResponse.INSUFFICIENT_RIGHTS;
  }

  private int createAuction(Bidder owner, String item, String description,
                            Price startingPrice, Price reservePrice) {

    int id;
    synchronized (idCounter) {
      id = idCounter.getAndIncrement();
    }
    AuctionImpl auction = new AuctionImpl(owner, id, item, description, startingPrice, reservePrice);
    auctions.put(auction.getId(), auction);

    return auction.getId();
  }

  private Auction getAuction(int id) {
    return auctions.get(id);
  }

  private List<Auction> getLiveAuctions() {
    return Collections.unmodifiableList(
        auctions.values()
            .stream()
            .filter(auction -> !auction.isClosed())
            .collect(Collectors.toList()));
  }

  public static void main(String[] args) throws Exception {
    new ClusterMember().start();
  }
}
