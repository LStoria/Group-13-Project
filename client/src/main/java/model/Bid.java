package model;

public class Bid {
    private final int itemId;
    private final double amount;
    private final String username;

    public Bid(int itemId, double amount, String username) {
        this.itemId = itemId;
        this.amount = amount;
        this.username = username;
    }

    public int getItemId() {
        return itemId;
    }

    public double getAmount() {
        return amount;
    }

    public String getUsername() {
        return username;
    }
}
