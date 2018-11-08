package AuctionClient;

import java.rmi.RemoteException;
import java.util.Scanner;

public class Buyer extends Client implements Runnable {

  public Buyer() {
    super();
  }

  private void browse() {
    System.out.println("browsing ...");
    try {
      System.out.println(server.getString());
    } catch (RemoteException e) {
      System.out.println("unable to call getString() from remote server");
    }
  }

  private void bid() {
    System.out.println("bidding ...");
  }

  @Override
  public void run() {
    Scanner sc = new Scanner(System.in);

    System.out.println("usage: TODO");

    while (true) {
      String input = sc.next().toLowerCase().trim();
      switch (input) {
        case "browse":
          browse();
          break;
        case "bid":
          bid();
          break;
        default:
          System.out.println("unknown command: " + input);
          break;
      }
    }
  }

  public static void main(String[] args) {
    new Buyer().run();
  }
}
