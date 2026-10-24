package restaurant.entity;

// Staff who can seat a queued customer: the Waiter and the Host
public interface Seater {

    String getRole();

    boolean hasActed();

    void seat(Customer customer, Table table);
}
