package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AuctionItem {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty imageBase64 = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final DoubleProperty startPrice = new SimpleDoubleProperty();
    private final DoubleProperty currentPrice = new SimpleDoubleProperty();
    private final StringProperty winner = new SimpleStringProperty();
    private final StringProperty seller = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty timeLeft = new SimpleIntegerProperty();

    public AuctionItem(int id, String name, double currentPrice, String winner) {
        this(id, name, "", currentPrice, currentPrice, winner, "", "ACTIVE", 0);
    }

    public AuctionItem(int id, String name, String type, double startPrice, double currentPrice,
                       String winner, String seller, String status, int timeLeft) {
        this(id, name, type, startPrice, currentPrice, winner, seller, status, timeLeft, "");
    }

    public AuctionItem(int id, String name, String type, double startPrice, double currentPrice,
                       String winner, String seller, String status, int timeLeft, String imageBase64) {
        this.id.set(id);
        this.name.set(name);
        this.type.set(type);
        this.startPrice.set(startPrice);
        this.currentPrice.set(currentPrice);
        this.winner.set(winner);
        this.seller.set(seller);
        this.status.set(status);
        this.timeLeft.set(timeLeft);
        this.imageBase64.set(imageBase64 != null ? imageBase64 : "");
    }
    public String getImageBase64() { return imageBase64.get(); }
    public void setImageBase64(String v) { imageBase64.set(v != null ? v : ""); }
    public StringProperty imageBase64Property() { return imageBase64; }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public double getStartPrice() {
        return startPrice.get();
    }

    public DoubleProperty startPriceProperty() {
        return startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice.get();
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice.set(currentPrice);
    }

    public DoubleProperty currentPriceProperty() {
        return currentPrice;
    }

    public String getWinner() {
        return winner.get();
    }

    public void setWinner(String winner) {
        this.winner.set(winner);
    }

    public StringProperty winnerProperty() {
        return winner;
    }

    public String getSeller() {
        return seller.get();
    }

    public StringProperty sellerProperty() {
        return seller;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public int getTimeLeft() {
        return timeLeft.get();
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft.set(timeLeft);
    }

    public IntegerProperty timeLeftProperty() {
        return timeLeft;
    }
}
