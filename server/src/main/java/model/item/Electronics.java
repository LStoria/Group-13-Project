package model.item;

import model.user.User;

public class Electronics extends Item {

    private String brand;

    public Electronics(String name, User seller, String description, double startPrice, double currentPrice, String brand) {
        super(name, seller, description, startPrice, currentPrice);
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }
}
