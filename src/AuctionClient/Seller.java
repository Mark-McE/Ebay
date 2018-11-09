package AuctionClient;

import java.util.Scanner;

public class Seller extends Client implements Runnable {

  private static final String usage = "TODO";

  public Seller() {
    super();
  }

  @Override
  public void run() {
    Scanner sc = new Scanner(System.in);
    sc.useDelimiter("\\R");

    System.out.println("usage: " + usage);

    while (true) {
      String input = sc.next().toLowerCase().trim();
      switch (input) {
        case "create auction":
          createAuction();
          break;
        case "usage":
          System.out.println("usage: " + usage);
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
