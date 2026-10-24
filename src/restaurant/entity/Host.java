package restaurant.entity;

public class Host extends Staff implements Seater {

    @Override
    public String getRole() {
        return "Host";
    }

    @Override
    public String getDuty() {
        return "seats at most one queued customer per turn, in addition to the Waiter's task";
    }

    @Override
    public void seat(Customer customer, Table table) {
        completeTask();
        table.seat(customer);
        customer.sitDown();
    }
}
