package restaurant.entity;

public class Waiter extends Staff {

    @Override
    public String getRole() {
        return "Waiter";
    }

    @Override
    public String getDuty() {
        return "one task per turn - seat a queued customer, take an order, or serve a READY dish";
    }

    public void seat(Customer customer, Table table) {
        completeTask();
        table.seat(customer);
        customer.sitDown();
    }

    public Order takeOrder(Customer customer, MenuItem item, int bill) {
        completeTask();
        Order order = new Order(customer, item, bill);
        customer.placeOrder(order);
        return order;
    }

    public void serve(Order order, int turn) {
        completeTask();
        order.markServed();
        order.getCustomer().startEating(turn);
    }
}
