package model.item;

import model.user.User;

public class Vehicle extends Item {

    private String brand;
    private String model;

    public Vehicle(String name, User seller, double startPrice, double currentPrice) {
        super(name, seller, startPrice, currentPrice);
        this.brand = "";
        this.model = "";
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String getType() {
        return "VEHICLE";
    }

}
