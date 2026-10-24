package restaurant.control;

import java.util.Map;
import java.util.TreeMap;

import restaurant.entity.CriticCustomer;
import restaurant.entity.Customer;
import restaurant.entity.Menu;
import restaurant.entity.MenuItem;
import restaurant.entity.Objective;
import restaurant.entity.RegularCustomer;
import restaurant.entity.VIPCustomer;

// The base setting from the brief. It never changes, so every run can be repeated.
public final class GameSetup {

    public static final int MAX_TURNS = 18;
    public static final int TABLES = 2;

    private GameSetup() {
    }

    public static GameController createBaseGame() {
        return new GameController(MAX_TURNS, TABLES, createMenu(), createArrivals(), createObjective());
    }

    // prices in cents
    public static Menu createMenu() {
        Menu menu = new Menu();
        menu.addItem(new MenuItem("Salad", 900, 1));
        menu.addItem(new MenuItem("Burger", 1500, 1));
        menu.addItem(new MenuItem("Pasta", 1800, 2));
        return menu;
    }

    // key = the turn the customer arrives on
    // a VIP and a Critic take the place of two Regulars, so there are still six arrivals
    public static Map<Integer, Customer> createArrivals() {
        Map<Integer, Customer> arrivals = new TreeMap<>();
        arrivals.put(1, new RegularCustomer(1, "Alice"));
        arrivals.put(4, new VIPCustomer(2, "Victor"));
        arrivals.put(7, new RegularCustomer(3, "Chloe"));
        arrivals.put(10, new CriticCustomer(4, "Carmen"));
        arrivals.put(13, new RegularCustomer(5, "Ethan"));
        arrivals.put(16, new RegularCustomer(6, "Fiona"));
        return arrivals;
    }

    // at least 4 paid customers, $45.00 revenue and an average satisfaction of 60
    public static Objective createObjective() {
        return new Objective(4, 4500, 60);
    }
}
