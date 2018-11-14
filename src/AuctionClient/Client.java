package AuctionClient;

import AuctionInterfaces.AuctionHouse;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public abstract class Client {

  protected static final String RMI_REMOTE_EXCEPTION_STIRNG
          = "Error: unable to communicate with Auction house server";

  protected final AuctionHouse server;
  protected final Scanner sc = new Scanner(System.in);

  public Client() {
    // Create the reference to the remote object through the rmiregistry
    AuctionHouse server = null;
    try {
      server = (AuctionHouse) Naming.lookup("rmi://localhost/AuctionHouse");
    } catch (NotBoundException | MalformedURLException | RemoteException e) {
      System.out.println("cannot connect to remote server");
      e.printStackTrace();
      System.exit(1);
    }

    this.server = server;

    sc.useDelimiter("\\R");
  }

  protected float inputCurrency(String inputMsg) {
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
}
