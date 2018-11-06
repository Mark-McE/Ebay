package AuctionServer;

import AuctionInterfaces.AuctionHouse;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class AuctionHouseServer {

    //Construct a new AuctionServer.AuctionHouseImpl object and bind it to the local rmiregistry
    //N.b. it is possible to host multiple objects on a server by repeating the
    //following method.
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
