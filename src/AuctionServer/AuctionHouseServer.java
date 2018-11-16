package AuctionServer;

import AuctionInterfaces.AuctionHouse;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Binds AuctionHouse to the rmi registry of localhost, and enables others
 * to access the auction house server to buy and sell on
 */
public class AuctionHouseServer {

  public AuctionHouseServer() {
    try {
      AuctionHouse auctionHouse = new AuctionHouseImpl();
      Naming.rebind("rmi://localhost/AuctionHouse", auctionHouse);
    } catch (RemoteException | MalformedURLException e) {
      System.out.println("unable to bind AuctionHouseImpl to rmiregistry");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void main(String args[]) {
    new AuctionHouseServer();
  }
}
