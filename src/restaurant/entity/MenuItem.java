package restaurant.entity;

public class MenuItem {

    private final String name;
    private final int price;      // in cents
    private final int prepUnits;

    public MenuItem(String name, int price, int prepUnits) {
        this.name = name;
        this.price = price;
        this.prepUnits = prepUnits;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getPrepUnits() {
        return prepUnits;
    }

    // "1 preparation unit" or "3 preparation units"
    public String getPrepUnitsText() {
        return prepUnits + (prepUnits == 1 ? " preparation unit" : " preparation units");
    }

    @Override
    public String toString() {
        return name;
    }
}
