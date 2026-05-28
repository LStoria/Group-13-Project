package model.item;

import model.user.User;

public abstract class Item extends Entity {

    protected String name;
    private User seller;
    protected double startPrice;
    protected double currentPrice;

    public Item(String name, User seller, String description, double startPrice, double currentPrice) {
        this.name = name;
        this.seller = seller;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
    }

    public String getName() {
        return name;
    }

    public User getSeller() {
        return seller;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", seller=" +
                (seller != null
                        ? seller.getUsername()
                        : "null") +
                ", startPrice=" + startPrice +
                ", currentPrice=" + currentPrice +
                '}';
    }

}
