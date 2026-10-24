package restaurant.util;

// All money is kept as int cents, e.g. 1830 is $18.30
public final class Money {

    private Money() {
    }

    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
