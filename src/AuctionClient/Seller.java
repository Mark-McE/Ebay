package AuctionClient;

import AuctionInterfaces.Auction;
import AuctionInterfaces.Bid;
import AuctionInterfaces.Price;

import java.rmi.RemoteException;

public class Seller extends Client implements Runnable {

  private static final String help =
      "Seller client for Auction House.\n"
      + "Available commands:\n"
      + "Create auction:\tCreate a new auction on the auction house. An auction"
      + "id will be provided upon creating an auction.\n"
      + "Close auction:\tClose an auction on the Auction house, the winning bidder "
      + "or reserve price will be quoted on close.\n"
      + "Help:\t\tPrints this help message.";

  public Seller() {
    super();
  }

  @Override
  public void run() {
    System.out.println(help);

    while (true) {
      System.out.print("\nAuction_house_seller_client>");
      String input = sc.next().toLowerCase().trim();
      try {
        switch (input) {
          case "create auction":
            createAuction();
            break;
          case "close auction":
            closeAuction();
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

  private void createAuction() throws RemoteException {
    String item;
    String description;
    Price startingPrice;
    Price reservePrice;

    System.out.println("Input auction information");
    System.out.print("Item name: ");
    item = sc.next().trim();

    System.out.print("Item description: ");
    description = sc.next().trim();

    startingPrice = inputPrice("Starting price in format £##.##: £");

    while (true) {
      reservePrice = inputPrice("Reserve price in format £##.##: £");
      if (reservePrice.toFloat() < startingPrice.toFloat())
        System.out.println("Error: reserve price cannot be lower than starting price");
      else
        break;
    }

    int id = server.createAuction(item, description, startingPrice, reservePrice);
    System.out.println("\nAuction created with id: " + id);
  }

  private void closeAuction() throws RemoteException {
    System.out.print("Auction id: ");
    String input = sc.next().trim();

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

    Auction closedAuction = server.closeAuction(id);

    if (closedAuction == null) {
      System.out.println("Error: Auction id not found (may already be closed)");
      return;
    }
    System.out.println("Auction " + id + " successfully closed");

    Bid winningBid = closedAuction.getWinningBid();
    if (winningBid == null) {
      System.out.printf(
          "Reserve price £%.2f not met, item not sold.\n",
          closedAuction.getReservePrice().toFloat());
    } else {
      System.out.printf(
          "Winning bid: £%.2f\n" +
          "Winning bidder: %s, (%s)\n",
          winningBid.toFloat(),
          winningBid.getBidderName(),
          winningBid.getBidderEmail());
    }
  }

  public static void main(String[] args) {
    new Seller().run();
  }
}
