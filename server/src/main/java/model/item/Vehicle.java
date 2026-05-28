package model.item;

import model.user.User;

public class Vehicle extends Item {

    private String brand;
    private String model;

    public Vehicle(String name, User seller, String description, double startPrice, double currentPrice, String brand, String model) {
        super(name, seller, description, startPrice, currentPrice);
        this.brand = brand;
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

}
