package AuctionClient;

import AuctionInterfaces.AuctionHouse;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Buyer implements Runnable {

    private final AuctionHouse server;
    public Buyer() {
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
            String input = sc.next();
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
        new Thread(new Buyer()).start();
    }
}
