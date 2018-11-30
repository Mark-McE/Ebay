package AuctionClient;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse.BidResponse;
import AuctionInterfaces.Bidder;
import AuctionInterfaces.Price;

import java.rmi.RemoteException;
import java.util.List;

/**
 *  A client program that enables a buyers to bid for auctioned items.
 *
 *  Enables buyers to browse the list of currently active auctions with their
 *  current highest bid (but not the reserve price, which is secret).
 *
 *  Enables buyers to bid for a selected item by entering the auction id and the
 *  buyer’s details (name and e-mail).
 *
 *  Gives clear messages to indicate whether a bid has been accepted or not,
 *  and why a bid has been rejected.
 */
public class Buyer extends Client {

  private static final String help =
      "Buyer client for Auction House.\n"
      + "Available commands:\n"
      + "Browse:\t\tPrints a human readable list of all current live auctions.\n"
      + "Bid:\t\tPlace a bid on a live auction by quoting it's auction id.\n"
      + "Help:\t\tPrints this help message.";

  private Bidder bidder = null;

  public Buyer() {
    super();
  }

  /**
   * Begins the command-line interface for this client.
   */
  public void run() {
    System.out.println(help);

    while (true) {
      System.out.print("\nAuction_house_buyer_client>");
      String input = sc.next().toLowerCase().trim();
      try {
        switch (input) {
          case "browse":
            browse();
            break;
          case "bid":
            bid();
            break;
          case "/?":
          case "help":
            System.out.println(help);
            break;
          default:
            System.out.println("unknown command: \"" + input
                + "\". For a list of commands, type help");
            break;
        }
      } catch (RemoteException e) {
        System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
        if (reconnectToServer())
          System.out.println("... Reconnected to Auction house server. Please try action again");
      }
    }
  }

  /**
   * Requests all live auctions from the server and prints them in a human readable
   * format
   */
  private void browse() throws RemoteException {
    List<Auction> auctions = server.getLiveAuctions();

    System.out.println("Auction listings:");
    for (Auction auction : auctions)
      System.out.println(auction.toReadableString() + "\n");
    System.out.println(auctions.size() + " total listings found");
  }

  /**
   * Takes user input then calls bid(id, price)
   */
  private void bid() throws RemoteException {
    if (bidder == null) {
      System.out.println("Error: user details not provided, please provide them"
          + " using the \"Set details\" command.");
      return;
    }

    System.out.print("Auction id: ");
    String input = sc.next();

    int id;
    if (!input.matches("[+-]?\\d+")) {
      System.out.println("Error: not a valid Auction id");
      return;
    }
    try {
      id = Integer.valueOf(input);
    } catch (NumberFormatException e) {
      // For input of |id| > int.maxValue
      System.out.println("Error: not a valid Auction id");
      return;
    }

    Price price = inputPrice("bid price: £");

    bid(id, price);
  }

  /**
   * performs a bid on the auctions defined by id.
   * Prints out the outcome of the bid to System.out which will be one of:
   *   bid successful
   *   bid too low
   *   auction closed
   *   auction not found
   * @param id The auction id to bid on
   * @param price The amount of money to bid.
   */
  public void bid(int id, Price price) throws RemoteException {
    if (bidder == null) {
      System.out.println("Error: user details not provided, please provide them"
          + " using the \"Set details\" command.");
      return;
    }

    BidResponse response = server.bid(id, bidder.createBid(price));

    switch (response) {
      case OK:
        System.out.println("Bid OK");
        break;
      case TOO_LOW:
        System.out.println("Error: Bid value lower than current best bid or starting price");
        break;
      case AUCTION_NOT_FOUND:
        System.out.println("Error: Auction id not found");
        break;
      case AUCTION_CLOSED:
        System.out.println("Error: Auction closed");
        break;
    }
  }

  public static void main(String[] args) {
    new Buyer().run();
  }
}
