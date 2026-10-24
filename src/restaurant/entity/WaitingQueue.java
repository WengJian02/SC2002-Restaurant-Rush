package restaurant.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Customers waiting for a table, in arrival order.
// The manager picks who gets seated, so it is not strictly first come first served.
public class WaitingQueue {

    private final List<Customer> customers = new ArrayList<>();

    public void add(Customer customer) {
        customers.add(customer);
    }

    public void remove(Customer customer) {
        customers.remove(customer);
    }

    public boolean contains(Customer customer) {
        return customers.contains(customer);
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }
}
