package AuctionClient;

import AuctionInterfaces.Auction;

import java.rmi.RemoteException;

public class Seller extends Client implements Runnable {

  private static final String usage = "TODO";

  public Seller() {
    super();
  }

  @Override
  public void run() {
    System.out.println("usage: " + usage);

    while (true) {
      System.out.print("\nAuction_house_seller_client>");
      String input = sc.next().toLowerCase().trim();
      switch (input) {
        case "create auction":
          createAuction();
          break;
        case "close auction":
          closeAuction();
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

  private void createAuction() {
    String item;
    String description;
    float startingPrice;
    float reservePrice;

    System.out.println("Input auction information");
    System.out.print("item name: ");
    item = sc.next().trim();

    System.out.print("item description: ");
    description = sc.next().trim();

    startingPrice = inputCurrency("starting price in format £##.##: £");

    do {
      reservePrice = inputCurrency("reserve price in format £##.##: £");
      if (reservePrice < startingPrice)
        System.out.println("Error: reserve price cannot be lower than starting price");
    } while (reservePrice < startingPrice);

    try {
      int id = server.createAuction(item, description, startingPrice, reservePrice);
      System.out.println("\nAuction created with id: " + id);
    } catch (RemoteException e) {
      System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
      return;
    }
  }

  private void closeAuction() {
    System.out.print("Auction id of Auction to close: ");
    String input = sc.next().trim();
    try {
      if (!input.chars().allMatch(Character::isDigit)
          || server.getListings().stream()
          .noneMatch(a -> a.getId() == Integer.valueOf(input))) {
        System.out.println("Error: not a valid Auction id");
        return;
      }
    } catch (RemoteException e) {
      System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
      return;
    }
    int id = Integer.valueOf(input);

    Auction closedAuction;
    try {
      closedAuction = server.closeAuction(id);
    } catch (RemoteException e) {
      System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
      return;
    }

    if (closedAuction == null) {
      // if here, the auction was closed between auction id input and closeAuction method call
      System.out.println("Error: Auction id no longer exist");
      return;
    }
    System.out.println("Auction " + id + " successfully closed");

    if (closedAuction.getWinnerName() == null) {
      System.out.println("Reserve price not met, item not sold.");
    } else {
      System.out.println(String.format(
          "Winning bid: £%.2f\n" +
          "Winning bidder: %s, (%s)",
          closedAuction.getWinnerBid(), closedAuction.getWinnerName(),
          closedAuction.getWinnerEmail()));
    }
  }

  public static void main(String[] args) {
    new Seller().run();
  }
}
