package AuctionServer.DataStructures;

import java.util.concurrent.atomic.AtomicInteger;

public class Auction {
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private final int id;
    private final String item;
    private final String description;
    private final float startingPrice;
    private final float reservePrice;

    public Auction(String item, String description, float startingPrice, float reservePrice) {
        if (String.valueOf(startingPrice).split("\\.")[1].length() > 2
        || String.valueOf(reservePrice).split("\\.")[1].length() > 2)
            throw new IllegalArgumentException("prices given have more than 2 decimal places");

        this.id = idCounter.getAndIncrement();
        this.item = item;
        this.description = description;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
    }

    public int getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public String getDescription() {
        return description;
    }

    public float getStartingPrice() {
        return startingPrice;
    }

    public float getReservePrice() {
        return reservePrice;
    }
}
