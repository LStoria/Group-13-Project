package model.item;

import model.user.User;

public class Electronics extends Item {

    private String brand;

    public Electronics(String name, User seller, double startPrice, double currentPrice, String brand) {
        super(name, seller, startPrice, currentPrice);
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public String getType() {
        return "ELECTRONICS";
    }

}
