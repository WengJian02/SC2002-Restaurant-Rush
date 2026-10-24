package restaurant.entity;

public enum OrderStatus {
    PREPARING,
    READY,
    SERVED,
    PAID,
    CANCELLED   // customer left before it was served
}
