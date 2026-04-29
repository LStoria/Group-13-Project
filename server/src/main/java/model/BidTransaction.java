package model;

public class BidTransaction extends Entity {

    private User bidder;
    private double amount;

    public BidTransaction(User bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

}
