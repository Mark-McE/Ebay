package AuctionServer;

import AuctionServer.ClusterMember.Service;
import org.jgroups.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static AuctionServer.ClusterMember.Service.RECOVER_STATE;

public class ClusterRequester extends ReceiverAdapter {
  private final Channel channel;
  private int coordResultIndex = 0;
  private final List<Object> results;

  public ClusterRequester(Channel channel) {
    this.channel = channel;
    try {
      channel.connect(ClusterMember.channelName);
      GMS gms = (GMS)channel.getProtocolStack().findProtocol(GMS.class);
      gms.setValue("use_delta_views", false);
    } catch (Exception e) {
      System.out.println("Unable to connect to the cluster");
      e.printStackTrace();
      System.exit(1);
    }
    channel.setReceiver(this);
    results = new CopyOnWriteArrayList<>();
  }

  @Override
  public void receive(Message msg) {
    System.out.println("[CR] msg received: " + msg);
    // messages sent to all nodes have dest == null
    if (!channel.getAddress().equals(msg.dest()))
      return;

    try {
      System.out.println("[CR] collected a result from a CM");
      results.add(Util.objectFromByteBuffer(msg.getBuffer()));
      // note the index of the result from the coordinator in case of result mismatches
      if (msg.src().equals(channel.getView().getCoord()))
        coordResultIndex = results.size()-1;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void viewAccepted(View view) {
    // never stay as the channel coordinator, as cluster state is not held here
    if (channel.getAddress().equals(view.getCoord()))
      changeCoord();
  }

  /**
   * Makes a request to all nodes in the cluster to perform a service
   * @param service the service each node will perform
   * @param timeout the maximum time to wait for a response from a node
   * @param params the arguments required to perform the service
   * @return The return value defined by the service
   */
  public synchronized Object requestService(Service service, int timeout, Object... params) throws Exception {
    if (params.length != service.params.length)
      throw new IllegalArgumentException("incorrect number of parameters for service");
    for (int i = 0; i < params.length; i++)
      if (!service.params[i].isInstance(params[i]))
        throw new IllegalArgumentException("parameter passed not of required type\n" +
            "passed: " + params[i].getClass() + ", required: " + service.params[i]);

    // prepare the message to send to the cluster
    byte[] payload = Util.objectToByteBuffer(new TripleImpl<>(service, params, channel.getAddress()));
    Message message = new Message(channel.getView().getCoord(), payload);

    // send the message to the cluster
    results.clear();
    System.out.println("[CR] sending request to coord " + message);
    int numExpectedResults = channel.getView().getMembersRaw().length-1;
    channel.send(message);

    // start a timeout in case of some nodes not responding
    final boolean[] isTimeout = {false};
    new Thread(() -> {
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException ignored) {
      } finally {
        isTimeout[0] = true;
      }
    }).start();

    while (results.size() < numExpectedResults && !isTimeout[0])
      ; // wait for results

    // if results from cluster members disagree, request all nodes to recover state
    // and return the result received from the coordinator
    if (results.stream().distinct().limit(2).count() > 1) {
      System.out.println("[CR] mismatched results from cluster members.. recovering state");
      Object retVal = results.get(coordResultIndex);

      // request all cluster members to recover a shared state
      payload = Util.objectToByteBuffer(new TripleImpl<>(RECOVER_STATE, null, null));
      message = new Message(channel.getView().getCoord(), payload);
      results.clear();
      System.out.println("[CR] requesting all CMs restore state " + message);
      channel.send(message);

      // return the value from the channel coordinator
      return retVal;
    }
    return results.stream().findAny()
        .orElseThrow(() -> new RuntimeException("no response from cluster"));
  }

  /**
   * Changes the channel coordinator to any other node
   * Can only be completed if the caller is the current channel coordinator
   * Exits the program if there are no other nodes in the cluster
   */
  private void changeCoord() {
    View view = channel.getView();
    if (!view.getCoord().equals(channel.getAddress())) {
      System.out.println("[CR] Attempting to elect a different coord when not the coord");
      return;
    }
    if (view.size() == 1) {
      System.out.println("[CR] no nodes in the cluster");
      System.exit(1);
    }
    List<Address> members = new ArrayList<>(view.getMembers());

    Address myAddress = members.remove(0);
    members.add(myAddress);
    View newView = new View(members.get(0), view.getViewId().getId()+1, members);
    GMS gms = (GMS)channel.getProtocolStack().findProtocol(GMS.class);
    System.out.println("[CR] changing coordinator");
    gms.castViewChange(newView, null, members);
  }
}