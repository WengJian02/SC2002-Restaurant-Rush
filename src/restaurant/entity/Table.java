package restaurant.entity;

// One customer per table. The table stays taken until that customer pays or leaves.
public class Table {

    private final int number;
    private Customer customer;   // null when the table is free

    public Table(int number) {
        this.number = number;
    }

    public boolean isFree() {
        return customer == null;
    }

    public void seat(Customer customer) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + number + " is already occupied.");
        }
        this.customer = customer;
    }

    public void free() {
        customer = null;
    }

    public int getNumber() {
        return number;
    }

    public Customer getCustomer() {
        return customer;
    }
}
