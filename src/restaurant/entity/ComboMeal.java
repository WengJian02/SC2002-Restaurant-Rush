package restaurant.entity;

import restaurant.util.Money;

// Two menu items sold together. Units are added up and the price is 90% of the two prices.
public class ComboMeal extends MenuItem {

    private static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                Money.percentOf(first.getPrice() + second.getPrice(), PRICE_PERCENT),
                first.getPrepUnits() + second.getPrepUnits());
        this.first = first;
        this.second = second;
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }
}
