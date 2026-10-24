package restaurant.entity;

import restaurant.util.Money;

public class VIPCustomer extends Customer {

    private static final int BILL_PERCENT = 90;   // 10% off

    public VIPCustomer(int number, String name) {
        super(number, name);
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLoss() {
        return 5;
    }

    @Override
    public int calculateBill(int menuPrice) {
        return Money.percentOf(menuPrice, BILL_PERCENT);
    }
}
