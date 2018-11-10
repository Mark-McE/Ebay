package AuctionClient;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse;

import java.rmi.RemoteException;

import static AuctionInterfaces.AuctionHouse.Response.*;

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
    int id;
    while (true) {
      System.out.print("Input Auction id of Auction to close: ");
      String input = sc.next().trim();
      try {
        if (!input.chars().allMatch(Character::isDigit)
            || server.getListings().stream().noneMatch((Auction a) -> a.getId() == Integer.valueOf(input))) {
          System.out.println("Error: not a valid Auction id");
          continue;
        }
      } catch (RemoteException e) {
        System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
        return;
      }
      id = Integer.valueOf(input);
      break;
    }

    Object[] response;
    try {
      response = server.closeAuction(id);
    } catch (RemoteException e) {
      System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
      return;
    }

    switch ((AuctionHouse.Response) response[0]) {
      case ID_NOT_FOUND:
        System.out.println("Error: Auction id not longer exists");
        return;
      case RESERVE_NOT_MET:
        break;
      case RESERVE_MET:
        break;
      default:
        break;
    }
    System.out.println("Auction " + id + " successfully closed");
    // TODO display winner or reserve price
  }

  private float inputCurrency(String inputMsg) {
    while (true) {
      System.out.print(inputMsg);
      String input = sc.next().trim();

      // matches any series of digits with optional decimal point and up to 2 digits after
      if (input.matches("\\d+(?:\\.\\d\\d?)?"))
        return Float.valueOf(input);

      if (input.charAt(0) == '-')
        System.out.println("Error: negative values not accepted");
      else
        System.out.println("Error: not a valid number");
    }
  }

  public static void main(String[] args) {
    new Seller().run();
  }
}
