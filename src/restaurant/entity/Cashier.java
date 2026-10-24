package restaurant.entity;

import java.util.List;

// Works automatically at the end of each turn
public class Cashier extends Staff {

    @Override
    public String getRole() {
        return "Cashier";
    }

    @Override
    public String getDuty() {
        return "automatic - charges at most one READY_TO_PAY customer per turn, oldest first";
    }

    // oldest first = whoever became READY_TO_PAY earliest. Returns null if nobody can pay this turn.
    public Customer chooseNextCustomer(List<Customer> customers, int turn) {
        Customer next = null;
        for (Customer customer : customers) {
            if (customer.canPay(turn)) {
                if (next == null || customer.getReadyToPayTurn() < next.getReadyToPayTurn()) {
                    next = customer;
                }
            }
        }
        return next;
    }

    // returns the amount collected, in cents
    public int collectPayment(Customer customer) {
        completeTask();
        customer.pay();
        return customer.getOrder().getBill();
    }
}
