package restaurant.entity;

// The three targets checked after the last turn. All three are needed to win.
public class Objective {

    private final int minPaidCustomers;
    private final int minRevenue;
    private final int minAverageSatisfaction;

    public Objective(int minPaidCustomers, int minRevenue, int minAverageSatisfaction) {
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenue = minRevenue;
        this.minAverageSatisfaction = minAverageSatisfaction;
    }

    public boolean isPaidCustomersMet(Scoreboard score) {
        return score.getPaidCount() >= minPaidCustomers;
    }

    public boolean isRevenueMet(Scoreboard score) {
        return score.getRevenue() >= minRevenue;
    }

    public boolean isSatisfactionMet(Scoreboard score) {
        return score.hasPayments() && score.getAverageSatisfaction() >= minAverageSatisfaction;
    }

    public boolean isMetBy(Scoreboard score) {
        return isPaidCustomersMet(score) && isRevenueMet(score) && isSatisfactionMet(score);
    }

    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }

    public int getMinRevenue() {
        return minRevenue;
    }

    public int getMinAverageSatisfaction() {
        return minAverageSatisfaction;
    }
}
