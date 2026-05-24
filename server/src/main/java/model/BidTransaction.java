package model;

import model.items.Entity;
import model.items.User;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {

    private final Auction auction;
    private User bidder;
    private double amount;
    private final LocalDateTime bidTime;

    public BidTransaction(
            Auction auction,
            User bidder,
            double amount,
            LocalDateTime bidTime
    ) {

        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.bidTime = bidTime;
    }

    public Auction getAuction() {
        return auction;
    }

    public User getBidder() {
        return bidder;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

}
