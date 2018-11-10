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
    float startingPrice = -1;
    float reservePrice = -1;

    System.out.println("Input auction information");
    System.out.print("item name: ");
    item = sc.next().trim();

    System.out.print("item description: ");
    description = sc.next().trim();

    startingPrice = inputCurrency("starting price in format £xx.xx: £");

    reservePrice = inputCurrency("reserve price in format £xx.xx: £");

    try {
      int id = server.createAuction(item, description, startingPrice, reservePrice);
      System.out.println("\nAuction created with id: " + id);
    } catch (RemoteException e) {
      System.out.println("unable to communicate with Auction house server");
      e.printStackTrace();
    }
  }

  private void closeAuction() {
    int id;
    while (true) {
      System.out.print("Input Auction id of Auction to close: ");
      String input = sc.next().trim();
      if (!input.chars().allMatch(Character::isDigit)) {
        System.out.println("Error: not a valid id");
        continue;
      }
      try {
        if (server.getListings()
            .stream()
            .noneMatch((Auction a) -> a.getId() == Integer.valueOf(input))) {
          System.out.println("Error: not a valid id");
          continue;
        }
      } catch (RemoteException e) {
        System.out.println("unable to communicate with Auction house server");
        e.printStackTrace();
      }
      id = Integer.valueOf(input);
      break;
    }

    System.out.println("Auction " + id + " successfully closed");
    // TODO display winner or reserve price
  }

  private float inputCurrency(String inputMsg) {
    float result = -1;
    do {
      System.out.print(inputMsg);
      try {
        result = Float.valueOf(sc.next());
      } catch (NumberFormatException e) {
        System.out.println("Error: not a number");
        continue;
      }
      if (result < 0)
        System.out.println("Error: negative values not accepted");
    } while (result < 0 || String.valueOf(result).split("\\.")[1].length() > 2);

    return result;
  }

  public static void main(String[] args) {
    new Seller().run();
  }
}
