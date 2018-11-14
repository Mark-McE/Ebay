package AuctionClient;

import AuctionInterfaces.Auction;
import AuctionInterfaces.AuctionHouse.BidResponse;
import AuctionInterfaces.Price;

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
      auctions = server.getLiveAuctions();
    } catch (RemoteException e) {
      System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
      return;
    }

    System.out.println("Auction listings:");

    for (Auction auction : auctions)
      System.out.println(auction.toReadableString() + "\n");
    System.out.println(auctions.size() + " total listings found");
  }

  private void bid() throws RemoteException {
    int id;
    Price price;
    String name;
    String email;
    BidResponse response;

    while (true) {
      System.out.print("auction id: ");
      String input = sc.next().trim();
      if (input.chars().allMatch(Character::isDigit)) {
        try {
          id = Integer.valueOf(input);
        } catch (NumberFormatException e) {
          // For input of id > int.maxValue
          System.out.print("Error: invalid auction id");
          continue;
        }
        break;
      }
      System.out.println("Error: invalid auction id");
    }

    price = inputPrice("bid price: £");

    System.out.print("Full name: ");
    name = sc.next().trim();

    System.out.print("Email: ");
    email = sc.next().trim();

    // TODO: implement below, restructure for saved bidder per instance of buyer
    // TODO: make all menu methods of buyer ans seller throw remoteException in method signature
    response = server.bid(id, server.createBid(server.createBidder(), price));

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
