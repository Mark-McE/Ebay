package AuctionServer;

import AuctionInterfaces.AuctionHouse;
import AuctionServer.DataStructures.Auction;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class AuctionHouseImpl
        extends java.rmi.server.UnicastRemoteObject
        implements AuctionHouse {

    private List<Auction> auctions = new ArrayList<>();

    public AuctionHouseImpl() throws java.rmi.RemoteException {
        super();
    }

    public String getString() throws java.rmi.RemoteException {
        return "a string";
    }

    @Override
    public int createAuction(String item, String description,
            float startingPrice, float reservePrice)
            throws RemoteException {

        Auction auction = new Auction(item, description, startingPrice, reservePrice);
        auctions.add(auction);

        return auction.getId();
    }
}
