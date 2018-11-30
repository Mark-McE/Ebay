package AuctionClient;

import AuctionInterfaces.AuctionHouse;
import AuctionInterfaces.Price;
import AuctionInterfaces.PriceFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * A parent class to Buyer and Seller which handles common functionality.
 *
 * Handles:
 *   initial connection to the Auction house server
 *   reconnecting to the auction house server at any time
 *   taking user input for prices
 */
public abstract class Client {

  protected static final String RMI_REMOTE_EXCEPTION_STIRNG
          = "Error: unable to communicate with Auction house server";

  protected final Scanner sc = new Scanner(System.in);

  protected AuctionHouse server;
  protected PriceFactory priceFactory;

  /**
   * initializes connection to the auction house server
   */
  public Client() {
    // Create the reference to the remote object through the rmiregistry
    try {
      server = (AuctionHouse) Naming.lookup("rmi://localhost/AuctionHouse");
      priceFactory = server.getPriceFactory();
    } catch (NotBoundException | MalformedURLException | RemoteException e) {
      System.out.println("Error: unable to communicate with Auction house server");
      System.exit(1);
    }

    sc.useDelimiter("\\R");
  }

  /**
   * Attempts to reconnect to the auction house server
   * @return true if reconnection successful, false otherwise.
   */
  protected boolean reconnectToServer() {
    try {
      server = (AuctionHouse) Naming.lookup("rmi://localhost/AuctionHouse");
      priceFactory = server.getPriceFactory();
    } catch (NotBoundException | MalformedURLException | RemoteException e) {
      System.out.println("Error: unable to reconnect to Auction house server");
      return false;
    }
    return true;
  }

  /**
   * Displays a message then takes user input for a price.
   * Does not accept
   *   values >= Integer.max_value/100
   *   negative values
   *   values with more than 2 decimal places
   *
   * @param inputMsg A string to display before requesting user input
   * @return The Price object containing the user inputted value
   */
  protected Price inputPrice(String inputMsg) {
    while (true) {
      System.out.print(inputMsg);
      String input = sc.next().trim();

      // matches any series of digits with optional decimal point and up to 2 digits after
      if (input.matches("\\d+(?:\\.\\d\\d?)?")) {
        Price price;
        try {
          price = priceFactory.createPrice((int) (Float.valueOf(input)*100));
        } catch (IllegalArgumentException e) {
          System.out.println("Error: not a valid price");
          continue;
        }

        return price;
      }

      System.out.println("Error: not a valid price");
    }
  }
}
