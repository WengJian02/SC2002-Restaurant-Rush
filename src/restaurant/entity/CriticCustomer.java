package restaurant.entity;

public class CriticCustomer extends Customer {

    public CriticCustomer(int number, String name) {
        super(number, name);
    }

    @Override
    public String getType() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLoss() {
        return 12;
    }

    // Our rule for the Critic: ignores the food rule and orders the item that needs the most
    // preparation units, to test the kitchen. If two items tie, the more expensive one.
    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        MenuItem choice = menu.getItem(0);
        for (MenuItem item : menu.getItems()) {
            boolean moreWork = item.getPrepUnits() > choice.getPrepUnits();
            boolean sameWorkButPricier = item.getPrepUnits() == choice.getPrepUnits()
                    && item.getPrice() > choice.getPrice();
            if (moreWork || sameWorkButPricier) {
                choice = item;
            }
        }
        return choice;
    }
}
