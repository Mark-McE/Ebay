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
      + "Set details:\tInput your name and email address. This is required to "
      + "use the bid command.\n"
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
          case "set details":
            setDetails();
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
   * Takes user input for the bidder's name and email address from the command line
   * Stores the inputted Name and Email within the bidder instance variable.
   * Bidder details must be set before making a bid.
   */
  private void setDetails() throws RemoteException {
    String name;
    String email;

    System.out.println("Input user details:\n");

    while (true) {
      System.out.print("First and last name: ");
      name = sc.next();

      // regular expression for a first and second name separated by a space
      // e.g. John Smith, James O'Connor
      // Does not accept more than 2 names e.g. john jacob jingleheimer schmidt
      if (name.matches("[A-Za-z][a-zA-z'-]*[a-zA-z] [A-Za-z][a-zA-z'-]*[a-zA-z]"))
        break;
      else
        System.out.println("Error: not a valid name. Example name: \"John Smith\"");
    }

    while (true) {
      System.out.print("Email address: ");
      email = sc.next();

      // regular expression for custom email address with 2-4 character top level domain
      if (email.matches("[a-zA-z][\\w\\.-]*@(?:[a-zA-z][\\w\\.-]+\\.)+[a-zA-z]{2,4}"))
        break;
      else
        System.out.println("Error: not a valid email address");
    }

    bidder = server.createBidder(name, email);
    System.out.println("Details successfully set");
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
   *
   * Bidder name and email address must be set using setDetails() first.
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
