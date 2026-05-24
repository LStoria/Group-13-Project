package model.items;

public class Vehicle extends Item {

    private String brand;
    private String model;

    public Vehicle(String name, double startPrice, String brand, String model) {
        super(name, startPrice);
        this.brand = brand;
        this.model = model;
    }

}
