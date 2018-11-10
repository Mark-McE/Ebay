package AuctionClient;

import AuctionInterfaces.Auction;

import java.rmi.RemoteException;
import java.util.List;

public class Buyer extends Client implements Runnable {

  private static final String usage = "TODO";

  public Buyer() {
    super();
  }

  @Override
  public void run() {
    System.out.println("usage: " + usage);

    while (true) {
      System.out.print("\nAuction_house_buyer_client>");
      String input = sc.next().toLowerCase().trim();
      switch (input) {
        case "browse":
          browse();
          break;
        case "bid":
          bid();
          break;
        case "/?":
        case "help":
        case "usage":
          System.out.println("usage: " + usage);
          break;
        default:
          System.out.println("unknown command: \"" + input
              + "\". For a list of commands, type help");
          break;
      }
    }
  }

  private void browse() {
    List<Auction> auctions = null;
    try {
      auctions = server.getListings();
    } catch (RemoteException e) {
      System.out.println("unable to communicate with Auction house server");
      e.printStackTrace();
      return;
    }

    System.out.println("Auction listings:");

    for (Auction auction : auctions)
      System.out.println(auction.toReadableString() + "\n");
    System.out.println(auctions.size() + " total listings found");
  }

  private void bid() {
    System.out.println("bidding ...");
  }

  public static void main(String[] args) {
    new Buyer().run();
  }
}
