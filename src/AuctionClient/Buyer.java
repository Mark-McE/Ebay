package AuctionClient;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse.BidResponse;
import AuctionInterfaces.Bidder;
import AuctionInterfaces.Price;

import java.rmi.RemoteException;
import java.util.List;

public class Buyer extends Client implements Runnable {

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

  @Override
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

  private void setDetails() throws RemoteException {
    String name;
    String email;

    System.out.println("Input user details:\n");

    while (true) {
      System.out.print("Full name: ");
      name = sc.next();

      if (name.matches("[A-Za-z][a-zA-z'-]*[a-zA-z] [A-Za-z][a-zA-z'-]*[a-zA-z]"))
        break;
      else
        System.out.println("Error: not a valid full name");
    }

    while (true) {
      System.out.print("Email address: ");
      email = sc.next();

      if (email.matches("[a-zA-z][\\w\\.-]*@(?:[a-zA-z][\\w\\.-]+\\.)+[a-zA-z]{2,4}"))
        break;
      else
        System.out.println("Error: not a valid email address");
    }

    bidder = server.createBidder(name, email);
    System.out.println("Details successfully set");
  }

  private void browse() throws RemoteException {
    List<Auction> auctions = server.getLiveAuctions();

    System.out.println("Auction listings:");
    for (Auction auction : auctions)
      System.out.println(auction.toReadableString() + "\n");
    System.out.println(auctions.size() + " total listings found");
  }

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

    BidResponse response = server.bid(id, bidder.createBid(price));

    switch (response) {
      case OK:
        System.out.println("Bid OK");
        break;
      case TOO_LOW:
        System.out.println("Error: Bid value lower than current best bid");
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
