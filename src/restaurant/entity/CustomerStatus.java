package restaurant.entity;

public enum CustomerStatus {
    WAITING_FOR_TABLE,  // in the waiting queue
    SEATED,             // at a table, not ordered yet
    WAITING_FOR_FOOD,
    EATING,
    READY_TO_PAY,
    PAID,
    LEFT                // satisfaction hit 0, left without paying
}
