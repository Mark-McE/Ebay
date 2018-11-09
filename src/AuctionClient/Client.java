package AuctionClient;

import AuctionInterfaces.AuctionHouse;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public abstract class Client {

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
}
