package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AuctionItem {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty currentPrice = new SimpleDoubleProperty();
    private final StringProperty winner = new SimpleStringProperty();

    public AuctionItem(int id, String name, double currentPrice, String winner) {
        this.id.set(id);
        this.name.set(name);
        this.currentPrice.set(currentPrice);
        this.winner.set(winner);
    }

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
}
