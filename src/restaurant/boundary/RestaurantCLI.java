package restaurant.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import restaurant.control.ActionResult;
import restaurant.control.GameController;
import restaurant.entity.Customer;
import restaurant.entity.MenuItem;
import restaurant.entity.Objective;
import restaurant.entity.Order;
import restaurant.entity.Scoreboard;
import restaurant.entity.Staff;
import restaurant.entity.Table;
import restaurant.util.Money;

// Text interface. It only prints the game and reads the manager's numbers.
// All the rules are checked in GameController.
public class RestaurantCLI {

    private static final String LINE = "==========================================================================";

    private final GameController game;
    private final Scanner input;
    private final boolean echo;   // print the answers too, so a replayed file reads like a real game

    public RestaurantCLI(GameController game, Scanner input, boolean echo) {
        this.game = game;
        this.input = input;
        this.echo = echo;
    }

    public void play() {
        showWelcome();
        while (!game.isGameOver()) {
            List<String> events = game.startTurn();
            showTurnStart(events);
            askWaiter();
            askChef();
            showTurnEnd(game.endTurn());
        }
        showFinalResult();
    }

    // the Waiter is always asked before the Chef

    private void askWaiter() {
        if (!game.waiterHasTask()) {
            game.waiterWait();
            System.out.println();
            System.out.println("WAITER: nothing to do this turn, so the Waiter waits.");
            return;
        }
        while (true) {
            System.out.println();
            System.out.println("WAITER - choose one task:");
            System.out.println("  1. Seat a customer from the waiting queue   [" + names(game.getCustomersToSeat()) + "]");
            System.out.println("  2. Take a seated customer's order           [" + names(game.getCustomersToOrder()) + "]");
            System.out.println("  3. Serve a READY dish                       [" + names(game.getCustomersToServe()) + "]");
            System.out.println("  0. Wait");
            int task = readChoice("Waiter task (0-3): ", 3);
            if (task == 0) {
                show(game.waiterWait());
                return;
            }
            int customer = readNumber("Customer number (0 = back): ");
            if (customer == 0) {
                continue;
            }
            ActionResult result;
            if (task == 1) {
                result = game.waiterSeat(customer);
            } else if (task == 2) {
                result = game.waiterTakeOrder(customer);
            } else {
                result = game.waiterServe(customer);
            }
            show(result);
            if (result.isSuccess()) {
                return;
            }
        }
    }

    private void askChef() {
        if (!game.chefHasTask()) {
            game.chefWait();
            System.out.println();
            System.out.println("CHEF: nothing to prepare this turn, so the Chef waits.");
            return;
        }
        while (true) {
            System.out.println();
            System.out.println("CHEF - prepare one unit of an order   [" + orders(game.getCustomersToCookFor()) + "]");
            int customer = readNumber("Customer number whose order to prepare (0 = wait): ");
            if (customer == 0) {
                show(game.chefWait());
                return;
            }
            ActionResult result = game.chefPrepare(customer);
            show(result);
            if (result.isSuccess()) {
                return;
            }
        }
    }

    private void show(ActionResult result) {
        if (result.isSuccess()) {
            System.out.println(">> " + result.getMessage());
        } else {
            System.out.println("!! Not allowed: " + result.getMessage() + " The task is not used up - choose again.");
        }
    }

    private void showWelcome() {
        Objective goal = game.getObjective();
        System.out.println(LINE);
        System.out.println("RESTAURANT RUSH - you are the manager");
        System.out.println(LINE);
        System.out.println("Staff:");
        for (Staff member : game.getStaff()) {
            System.out.println("  " + member.getRole() + ": " + member.getDuty());
        }
        System.out.println("Menu:");
        for (MenuItem item : game.getMenu().getItems()) {
            System.out.println("  " + item.getName() + " - " + Money.format(item.getPrice()) + ", "
                    + item.getPrepUnitsText());
        }
        System.out.println("A customer arrives at the start of turns " + joinTurns(game.getArrivalTurns())
                + " and chooses their own food.");
        System.out.println("Customers waiting for a table or for food lose satisfaction every turn and leave at 0.");
        System.out.println("Goal after " + game.getMaxTurns() + " turns: at least " + goal.getMinPaidCustomers()
                + " paid customers, revenue of at least " + Money.format(goal.getMinRevenue())
                + " and average satisfaction of paid customers of at least " + goal.getMinAverageSatisfaction() + ".");
        System.out.println("Answer each question with a number. Customers are numbered in order of arrival (#1, #2, ...).");
    }

    private void showTurnStart(List<String> events) {
        Scoreboard score = game.getScoreboard();
        System.out.println();
        System.out.println(LINE);
        System.out.println("TURN " + game.getTurn() + " of " + game.getMaxTurns()
                + "  |  Revenue " + Money.format(score.getRevenue())
                + "  |  Average satisfaction of paid customers " + average(score));
        System.out.println(LINE);
        for (String event : events) {
            System.out.println("* " + event);
        }
        System.out.println("Waiting queue:");
        if (game.getWaitingCustomers().isEmpty()) {
            System.out.println("  (empty)");
        }
        for (Customer customer : game.getWaitingCustomers()) {
            System.out.println("  " + describe(customer));
        }
        System.out.println("Tables:");
        for (Table table : game.getTables()) {
            String occupant = table.isFree() ? "free" : describe(table.getCustomer());
            System.out.println("  Table " + table.getNumber() + ": " + occupant);
        }
        System.out.println("Staff tasks available:");
        System.out.println("  Waiter : seat [" + names(game.getCustomersToSeat())
                + "]  take order [" + names(game.getCustomersToOrder())
                + "]  serve [" + names(game.getCustomersToServe()) + "]");
        System.out.println("  Chef   : prepare [" + orders(game.getCustomersToCookFor()) + "]");
        Customer nextToPay = game.getNextCustomerToPay();
        System.out.println("  Cashier: " + (nextToPay == null
                ? "nobody is ready to pay"
                : "charges " + nextToPay.getName() + " automatically at the end of this turn"));
    }

    private void showTurnEnd(List<String> events) {
        Scoreboard score = game.getScoreboard();
        System.out.println();
        System.out.println("--- End of turn " + game.getTurn() + " ---");
        for (String event : events) {
            System.out.println("* " + event);
        }
        System.out.println("Served " + score.getServedCount()
                + " | Paid " + score.getPaidCount()
                + " | Unhappy departures " + score.getUnhappyDepartures()
                + " | Revenue " + Money.format(score.getRevenue())
                + " | Average satisfaction " + average(score)
                + " | Turns used " + game.getTurn() + "/" + game.getMaxTurns());
    }

    private void showFinalResult() {
        Scoreboard score = game.getScoreboard();
        Objective goal = game.getObjective();
        System.out.println();
        System.out.println(LINE);
        System.out.println("GAME OVER - all " + game.getMaxTurns() + " turns played");
        System.out.println(LINE);
        System.out.println("Paid customers       : " + score.getPaidCount()
                + "  (target at least " + goal.getMinPaidCustomers() + ")  " + met(goal.isPaidCustomersMet(score)));
        System.out.println("Revenue              : " + Money.format(score.getRevenue())
                + "  (target at least " + Money.format(goal.getMinRevenue()) + ")  " + met(goal.isRevenueMet(score)));
        System.out.println("Average satisfaction : " + average(score)
                + "  (target at least " + goal.getMinAverageSatisfaction() + ")  " + met(goal.isSatisfactionMet(score)));
        System.out.println("Served " + score.getServedCount() + " | Unhappy departures " + score.getUnhappyDepartures());
        if (game.isVictory()) {
            System.out.println("RESULT: VICTORY - all three targets were met!");
        } else {
            System.out.println("RESULT: DEFEAT - not every target was met.");
        }
    }

    // e.g. "#3 Chloe (Regular) - satisfaction 84 - WAITING_FOR_FOOD - Pasta $18.00 PREPARING 1/2"
    private String describe(Customer customer) {
        String text = customer + " - satisfaction " + customer.getSatisfaction() + " - " + customer.getStatus();
        Order order = customer.getOrder();
        if (order != null) {
            text += " - " + order.getItem().getName() + " " + Money.format(order.getBill()) + " " + order.getStatus();
            if (order.isPreparing()) {
                text += " " + order.getUnitsDone() + "/" + order.getItem().getPrepUnits();
            }
        }
        return text;
    }

    private String names(List<Customer> customers) {
        if (customers.isEmpty()) {
            return "none";
        }
        List<String> parts = new ArrayList<>();
        for (Customer customer : customers) {
            parts.add("#" + customer.getNumber() + " " + customer.getName());
        }
        return String.join(", ", parts);
    }

    private String orders(List<Customer> customers) {
        if (customers.isEmpty()) {
            return "none";
        }
        List<String> parts = new ArrayList<>();
        for (Customer customer : customers) {
            Order order = customer.getOrder();
            parts.add("#" + customer.getNumber() + " " + customer.getName() + ": " + order.getItem().getName()
                    + " " + order.getUnitsDone() + "/" + order.getItem().getPrepUnits());
        }
        return String.join(", ", parts);
    }

    private String joinTurns(List<Integer> turns) {
        List<String> parts = new ArrayList<>();
        for (Integer turn : turns) {
            parts.add(String.valueOf(turn));
        }
        return String.join(", ", parts);
    }

    // one decimal place, or N/A until someone has paid
    private String average(Scoreboard score) {
        if (!score.hasPayments()) {
            return "N/A";
        }
        return String.format(Locale.US, "%.1f", score.getAverageSatisfaction());
    }

    private String met(boolean targetMet) {
        return targetMet ? "MET" : "NOT MET";
    }

    // keeps asking until the answer is between 0 and max
    private int readChoice(String prompt, int max) {
        while (true) {
            int choice = readNumber(prompt);
            if (choice >= 0 && choice <= max) {
                return choice;
            }
            System.out.println("Please enter a number from 0 to " + max + ".");
        }
    }

    private int readNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = nextAnswer();
            if (line == null) {
                // out of input (end of a replayed file), so answer 0 = wait / back
                System.out.println("0   (no more input)");
                return 0;
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("\"" + line + "\" is not a number. Please enter a number.");
            }
        }
    }

    // In the replay files anything after // is a note, and lines with only a note are skipped
    private String nextAnswer() {
        while (input.hasNextLine()) {
            String line = input.nextLine();
            int note = line.indexOf("//");
            if (note >= 0) {
                if (line.substring(0, note).trim().isEmpty()) {
                    continue;
                }
                line = line.substring(0, note);
            }
            line = line.trim();
            if (echo) {
                System.out.println(line);
            }
            return line;
        }
        return null;
    }
}
