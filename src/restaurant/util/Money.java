package restaurant.util;

// All money is kept as int cents, e.g. 1830 is $18.30
public final class Money {

    private Money() {
    }

    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    // rounded to the nearest cent, e.g. 90% of 1500 -> 1350
    public static int percentOf(int cents, int percent) {
        return (int) Math.round(cents * percent / 100.0);
    }
}
