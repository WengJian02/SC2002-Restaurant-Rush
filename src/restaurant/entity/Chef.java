package restaurant.entity;

public class Chef extends Staff {

    @Override
    public String getRole() {
        return "Chef";
    }

    @Override
    public String getDuty() {
        return "one preparation unit of one order per turn";
    }

    public void prepare(Order order) {
        completeTask();
        order.prepareOneUnit();
    }
}
