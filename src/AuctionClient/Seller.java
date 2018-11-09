package AuctionClient;

import java.util.Scanner;

public class Seller extends Client implements Runnable {

  public Seller() {
    super();
  }

  @Override
  public void run() {
    Scanner sc = new Scanner(System.in);
    sc.useDelimiter("\\R");

    System.out.println("usage: TODO");

    while (true) {
      String input = sc.next().toLowerCase().trim();
      switch (input) {
        case "create auction":
          createAuction();
          break;
        default:
          System.out.println("unknown command: " + input);
          break;
      }
    }
  }

  private void createAuction() {
    System.out.println("created new auction with id: 0");
  }

  public static void main(String[] args) {
    new Seller().run();
  }
}
