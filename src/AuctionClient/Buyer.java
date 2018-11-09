package AuctionClient;

import java.rmi.RemoteException;
import java.util.Scanner;

public class Buyer extends Client implements Runnable {

  private static final String usage = "TODO";

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
    sc.useDelimiter("\\R");

    System.out.println("usage: " + usage);

    while (true) {
      String input = sc.next().toLowerCase().trim();
      switch (input) {
        case "browse":
          browse();
          break;
        case "bid":
          bid();
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

  public static void main(String[] args) {
    new Buyer().run();
  }
}
