package AuctionServer;

import AuctionInterfaces.AuctionHouse;

public class AuctionHouseImpl
        extends java.rmi.server.UnicastRemoteObject
        implements AuctionHouse {

    public AuctionHouseImpl() throws java.rmi.RemoteException {
        super();
    }

    public String getString() throws java.rmi.RemoteException {
        return "a string";
    }
}
