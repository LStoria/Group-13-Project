package model.item;

import model.user.User;

public class Art extends Item {

    private String artist;

    public Art(String name, User seller, double startPrice, double currentPrice) {
        super(name, seller, startPrice, currentPrice);
        this.artist = "";
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public String getType() {
        return "ART";
    }

}
