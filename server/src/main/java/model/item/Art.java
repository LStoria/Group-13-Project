package model.item;

import model.user.User;

public class Art extends Item {

    private String artist;

    public Art(String name, User seller, String description, double startPrice, double currentPrice, String artist) {
        super(name, seller, description, startPrice, currentPrice);
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

}
