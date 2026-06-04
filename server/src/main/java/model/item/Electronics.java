package model.item;

import model.user.User;

public class Electronics extends Item {

    private String brand;

    public Electronics(String name, User seller, double startPrice, double currentPrice) {
        super(name, seller, startPrice, currentPrice);
        this.brand = "";
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public String getType() {
        return "ELECTRONICS";
    }

}
