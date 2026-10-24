package restaurant.entity;

import restaurant.util.Money;

// Can be switched on once per game, at the start of a turn, and lasts that turn and the next.
// Orders taken while it is on get 20% off, and waiting customers recover 15 satisfaction each turn.
public class HappyHour {

    public static final int DURATION = 2;
    public static final int DISCOUNT_PERCENT = 20;
    public static final int RECOVERY = 15;

    private boolean used;
    private int turnsLeft;

    public boolean isAvailable() {
        return !used;
    }

    public void activate() {
        if (used) {
            throw new IllegalStateException("Happy Hour can only be switched on once per game.");
        }
        used = true;
        turnsLeft = DURATION;
    }

    public boolean isActive() {
        return turnsLeft > 0;
    }

    public int getTurnsLeft() {
        return turnsLeft;
    }

    // applied after the customer's own discount
    public int applyDiscount(int bill) {
        if (isActive()) {
            return Money.percentOf(bill, 100 - DISCOUNT_PERCENT);
        }
        return bill;
    }

    // called at the end of each turn while it is on
    public void endTurn() {
        if (turnsLeft > 0) {
            turnsLeft--;
        }
    }
}
