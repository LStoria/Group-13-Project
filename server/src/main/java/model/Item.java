package model;

public abstract class Item extends Entity {

    protected String name;
    protected double startPrice;
    protected double currentPrice;

    public Item(String name, double startPrice) {
        this.name = name;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double price) {
        this.currentPrice = price;
    }

}
