package restaurant.entity;

// Revenue only goes up when someone pays, so unpaid or abandoned orders never count
public class Scoreboard {

    private int revenue;   // in cents
    private int servedCount;
    private int paidCount;
    private int paidSatisfactionTotal;
    private int unhappyDepartures;

    public void recordServed() {
        servedCount++;
    }

    // satisfaction is recorded at the moment the customer pays
    public void recordPayment(int amount, int satisfaction) {
        revenue += amount;
        paidCount++;
        paidSatisfactionTotal += satisfaction;
    }

    public void recordUnhappyDeparture() {
        unhappyDepartures++;
    }

    public boolean hasPayments() {
        return paidCount > 0;
    }

    // average of the paid customers, 0 if nobody has paid yet
    public double getAverageSatisfaction() {
        if (paidCount == 0) {
            return 0;
        }
        return (double) paidSatisfactionTotal / paidCount;
    }

    public int getRevenue() {
        return revenue;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }
}
