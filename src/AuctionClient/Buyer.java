package AuctionClient;

import AuctionInterfaces.Auction;
import AuctionInterfaces.BidResponse;

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
    List<Auction> auctions;
    try {
      auctions = server.getListings();
    } catch (RemoteException e) {
      System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
      return;
    }

    System.out.println("Auction listings:");

    for (Auction auction : auctions)
      System.out.println(auction.toReadableString() + "\n");
    System.out.println(auctions.size() + " total listings found");
  }

  private void bid() {
    int id;
    float price;
    String name;
    String email;
    BidResponse response;

    while (true) {
      System.out.print("auction id: ");
      String input = sc.next().trim();
      if (input.chars().allMatch(Character::isDigit)) {
        id = Integer.valueOf(input); // TODO catch numberFormatException for input of int.maxValue+1
        break;
      }
      System.out.println("Error: invalid auction id format");
    }

    price = inputCurrency("bid price: £");

    System.out.print("Full name: ");
    name = sc.next().trim();

    System.out.print("Email: ");
    email = sc.next().trim();

    try {
      response = server.bid(id, price, name, email);
    } catch (RemoteException e) {
      System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
      return;
    }
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
    }
  }

  public static void main(String[] args) {
    new Buyer().run();
  }
}
