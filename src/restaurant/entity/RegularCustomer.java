package restaurant.entity;

public class RegularCustomer extends Customer {

    public RegularCustomer(int number, String name) {
        super(number, name);
    }

    @Override
    public String getType() {
        return "Regular";
    }

    @Override
    public int getSatisfactionLoss() {
        return 8;
    }
}
