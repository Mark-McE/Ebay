package AuctionInterfaces;

public interface AuctionHouse extends java.rmi.Remote {
    String getString() throws java.rmi.RemoteException;

    int createAuction(String item, String description, float startingPrice, float reservePrice)
            throws java.rmi.RemoteException;
}
