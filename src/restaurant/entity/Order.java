package restaurant.entity;

// The bill is fixed when the order is taken
public class Order {

    private final Customer customer;
    private final MenuItem item;
    private final int bill;
    private int unitsDone;
    private OrderStatus status;

    public Order(Customer customer, MenuItem item, int bill) {
        this.customer = customer;
        this.item = item;
        this.bill = bill;
        this.unitsDone = 0;
        this.status = OrderStatus.PREPARING;
    }

    // the Chef does one unit per turn, the order is READY after the last one
    public void prepareOneUnit() {
        if (status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Only an order that is still PREPARING can be worked on.");
        }
        unitsDone++;
        if (unitsDone == item.getPrepUnits()) {
            status = OrderStatus.READY;
        }
    }

    public void markServed() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("Only a READY order can be served.");
        }
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Only a SERVED order can be paid.");
        }
        status = OrderStatus.PAID;
    }

    // the customer left before the food was served
    public void cancel() {
        if (status != OrderStatus.PREPARING && status != OrderStatus.READY) {
            throw new IllegalStateException("Only an order that has not been served can be cancelled.");
        }
        status = OrderStatus.CANCELLED;
    }

    public boolean isPreparing() {
        return status == OrderStatus.PREPARING;
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getBill() {
        return bill;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
