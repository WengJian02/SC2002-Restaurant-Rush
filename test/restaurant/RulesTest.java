package restaurant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import restaurant.boundary.RestaurantCLI;
import restaurant.control.ActionResult;
import restaurant.control.GameController;
import restaurant.control.GameSetup;
import restaurant.entity.Cashier;
import restaurant.entity.Customer;
import restaurant.entity.CustomerStatus;
import restaurant.entity.Menu;
import restaurant.entity.MenuItem;
import restaurant.entity.Objective;
import restaurant.entity.Order;
import restaurant.entity.OrderStatus;
import restaurant.entity.RegularCustomer;
import restaurant.entity.Scoreboard;
import restaurant.entity.Staff;
import restaurant.entity.Table;
import restaurant.entity.Waiter;
import restaurant.util.Money;

// Checks the rules from the brief one by one, then replays the saved runs in runs/stage-1
// through the real CLI. Run it from the project folder with test.sh or test.bat.
public class RulesTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        baseSetting();
        sectionThreeThreeExample();
        waiterDoesOneTaskPerTurn();
        refusedChoicesExplainWhyAndKeepTheTask();
        waiterActsBeforeChef();
        tablesStayOccupiedUntilPaymentOrDeparture();
        eatingAndPaymentFollowTheOrder();
        cashierChargesOneCustomerPerTurnOldestFirst();
        waitingAndAbandonment();
        customersChooseTheirOwnFood();
        lastArrivalCannotPayInTime();
        objectiveNeedsAllThreeTargets();
        moneyIsShownWithTwoDecimals();
        badInputIsHandled();
        replay("runs/stage-1/victory.txt", true, 5, 6600, "79.2");
        replay("runs/stage-1/defeat.txt", false, 3, 4200, "65.3");

        System.out.println();
        System.out.println(passed + " checks passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void baseSetting() {
        section("Base setting (Section 2.1)");
        GameController game = GameSetup.createBaseGame();
        check(game.getMaxTurns() == 18, "18 turns");
        check(game.getTables().size() == 2, "2 tables");
        check(roles(game.getStaff()).equals(Arrays.asList("Waiter", "Chef", "Cashier")), "staff: one Waiter, one Chef and one Cashier");
        check(game.getArrivalTurns().equals(Arrays.asList(1, 4, 7, 10, 13, 16)),
                "one customer arrives at the start of turns 1, 4, 7, 10, 13 and 16");
        Menu menu = game.getMenu();
        check(menu.size() == 3
                        && isItem(menu.getItem(0), "Salad", 900, 1)
                        && isItem(menu.getItem(1), "Burger", 1500, 1)
                        && isItem(menu.getItem(2), "Pasta", 1800, 2),
                "menu: Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units)");
        Objective goal = game.getObjective();
        check(goal.getMinPaidCustomers() == 4 && goal.getMinRevenue() == 4500 && goal.getMinAverageSatisfaction() == 60,
                "objective: at least 4 paid customers, $45.00 revenue and average satisfaction 60");
        boolean allRegular = true;
        for (Customer customer : GameSetup.createArrivals().values()) {
            allRegular = allRegular && customer.getType().equals("Regular");
        }
        check(allRegular, "Stage 1 customers are all Regular");
        game.startTurn();
        check(game.getWaitingCustomers().size() == 1 && game.getWaitingCustomers().get(0).getSatisfaction() == 100,
                "turn 1: the arriving customer joins the waiting queue with satisfaction 100");
    }

    private static void sectionThreeThreeExample() {
        section("Section 3.3 example: one Regular customer orders Pasta and gets full attention");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        Customer pat = new RegularCustomer(3, "Pat");   // customer #3 orders the 3rd menu item: Pasta
        arrivals.put(1, pat);
        GameController game = gameWith(arrivals);

        game.startTurn();   // turn 1: arrives, Waiter seats
        game.waiterSeat(3);
        game.endTurn();
        check(pat.getSatisfaction() == 92, "turn 1: seated, not served -> 92");

        game.startTurn();   // turn 2: Waiter takes the order, Chef does unit 1 of 2
        ActionResult order = game.waiterTakeOrder(3);
        game.chefPrepare(3);
        game.endTurn();
        check(order.getMessage().contains("Pasta") && order.getMessage().contains("$18.00"),
                "turn 2: the customer's choice (Pasta, $18.00) is shown when the order is placed");
        check(pat.getOrder().getUnitsDone() == 1 && pat.getSatisfaction() == 84, "turn 2: unit 1 of 2 done, satisfaction 84");

        game.startTurn();   // turn 3: unit 2 of 2, READY, cannot be served this turn
        check(!game.waiterServe(3).isSuccess(), "turn 3: the Waiter cannot serve a dish that is not READY");
        game.waiterWait();
        game.chefPrepare(3);
        check(pat.getOrder().isReady(), "turn 3: the Pasta becomes READY after its last unit");
        check(!game.waiterServe(3).isSuccess(), "turn 3: a dish finished this turn is not served this turn");
        game.endTurn();
        check(pat.getSatisfaction() == 76, "turn 3: still waiting for food -> 76");

        game.startTurn();   // turn 4: served
        check(game.waiterServe(3).isSuccess(), "turn 4: the Waiter serves the READY dish");
        game.endTurn();
        check(pat.getOrder().getStatus() == OrderStatus.SERVED && pat.getSatisfaction() == 76,
                "turn 4: order SERVED, no further loss (76)");

        game.startTurn();   // turn 5: finishes eating
        game.endTurn();
        check(pat.getStatus() == CustomerStatus.READY_TO_PAY && pat.getSatisfaction() == 76,
                "turn 5: finished eating -> READY_TO_PAY, satisfaction still 76");
        check(game.getScoreboard().getPaidCount() == 0, "turn 5: not charged on the turn they became READY_TO_PAY");

        game.startTurn();   // turn 6: pays
        game.endTurn();
        Scoreboard score = game.getScoreboard();
        check(pat.getStatus() == CustomerStatus.PAID && pat.getOrder().getStatus() == OrderStatus.PAID,
                "turn 6: customer and order PAID");
        check(score.getRevenue() == 1800 && score.getAverageSatisfaction() == 76,
                "turn 6: $18.00 added to revenue and satisfaction 76 recorded");
        check(game.getTables().get(0).isFree(), "turn 6: the table is freed");
    }

    private static void waiterDoesOneTaskPerTurn() {
        section("The Waiter completes at most one task per turn");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        arrivals.put(1, new RegularCustomer(1, "Ann"));
        arrivals.put(2, new RegularCustomer(2, "Bob"));
        GameController game = gameWith(arrivals);
        game.startTurn();
        game.waiterWait();
        game.endTurn();

        game.startTurn();
        check(game.waiterSeat(1).isSuccess(), "the first task of the turn is allowed");
        ActionResult second = game.waiterSeat(2);
        check(!second.isSuccess() && second.getMessage().contains("already done a task"), "a second task in the same turn is refused");
        check(game.getWaitingCustomers().size() == 1, "the second customer is still waiting in the queue");

        Waiter waiter = new Waiter();
        waiter.startTurn();
        waiter.waitThisTurn();
        check(throwsIllegalState(waiter::waitThisTurn), "the Staff class itself never allows two tasks in one turn");
    }

    private static void refusedChoicesExplainWhyAndKeepTheTask() {
        section("A refused choice explains why and does not use up the task");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        arrivals.put(1, new RegularCustomer(1, "Ann"));
        GameController game = gameWith(arrivals);
        game.startTurn();

        ActionResult order = game.waiterTakeOrder(1);
        check(!order.isSuccess() && order.getMessage().contains("WAITING_FOR_TABLE"),
                "taking an order from a customer still in the queue is refused, with the reason");
        ActionResult serve = game.waiterServe(1);
        check(!serve.isSuccess() && serve.getMessage().contains("has not ordered yet"),
                "serving a customer who has not ordered is refused, with the reason");
        ActionResult seat = game.waiterSeat(5);
        check(!seat.isSuccess() && seat.getMessage().contains("#5"), "seating a customer who is not in the queue is refused");
        check(game.waiterHasTask(), "after the refused choices the Waiter's task is still available");
        check(game.waiterSeat(1).isSuccess(), "the Waiter can then seat the customer");
    }

    private static void waiterActsBeforeChef() {
        section("Kitchen and serving: the Waiter acts before the Chef");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        arrivals.put(1, new RegularCustomer(1, "Ann"));   // orders Salad (1 unit)
        GameController game = gameWith(arrivals);
        game.startTurn();
        game.waiterSeat(1);
        game.endTurn();

        game.startTurn();
        ActionResult early = game.chefPrepare(1);
        check(!early.isSuccess() && early.getMessage().contains("Waiter acts before the Chef"),
                "the Chef cannot act before the Waiter has acted");
        game.waiterTakeOrder(1);
        check(game.chefPrepare(1).isSuccess(), "the Chef may work on an order the Waiter took this turn");
        check(!game.chefPrepare(1).isSuccess(), "the Chef advances only one preparation unit per turn");
        game.endTurn();

        game.startTurn();
        check(game.waiterServe(1).isSuccess(), "a dish that was READY before the Waiter acts can be served");
    }

    private static void tablesStayOccupiedUntilPaymentOrDeparture() {
        section("Tables stay occupied until their customer pays or leaves");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        arrivals.put(1, new RegularCustomer(1, "Ann"));
        arrivals.put(2, new RegularCustomer(2, "Bob"));
        arrivals.put(3, new RegularCustomer(3, "Cat"));
        GameController game = gameWith(arrivals);
        for (int customer = 1; customer <= 2; customer++) {
            game.startTurn();
            game.waiterSeat(customer);
            game.endTurn();
        }
        game.startTurn();
        ActionResult third = game.waiterSeat(3);
        check(!third.isSuccess() && third.getMessage().contains("Every table is occupied"),
                "seating is refused while both tables are occupied");

        Table table = new Table(1);
        table.seat(new RegularCustomer(1, "Ann"));
        check(throwsIllegalState(() -> table.seat(new RegularCustomer(2, "Bob"))),
                "the Table class itself refuses a second customer");
    }

    private static void eatingAndPaymentFollowTheOrder() {
        section("A customer cannot eat before being served");
        Customer ann = new RegularCustomer(1, "Ann");
        ann.sitDown();
        Order order = new Order(ann, new MenuItem("Salad", 900, 1), 900);
        ann.placeOrder(order);
        check(!ann.finishEating(5) && ann.getStatus() == CustomerStatus.WAITING_FOR_FOOD,
                "a customer who has not been served does not finish eating");
        check(throwsIllegalState(order::markServed), "an order that is not READY cannot be served");
        order.prepareOneUnit();
        check(throwsIllegalState(order::prepareOneUnit), "a READY order needs no more preparation");
    }

    private static void cashierChargesOneCustomerPerTurnOldestFirst() {
        section("The Cashier charges at most one READY_TO_PAY customer per turn, oldest first");
        Customer first = readyToPayOnTurn(new RegularCustomer(1, "Ann"), 4);
        Customer second = readyToPayOnTurn(new RegularCustomer(2, "Bob"), 5);
        Cashier cashier = new Cashier();
        check(cashier.chooseNextCustomer(Arrays.asList(second, first), 6) == first,
                "the customer who has been READY_TO_PAY the longest is charged first");
        check(cashier.chooseNextCustomer(Arrays.asList(second), 5) == null,
                "a customer who became READY_TO_PAY this turn pays next turn at the earliest");
        cashier.startTurn();
        int amount = cashier.collectPayment(first);
        check(amount == 900 && first.getStatus() == CustomerStatus.PAID && first.getOrder().getStatus() == OrderStatus.PAID,
                "payment returns the bill and marks the customer and order PAID");
        check(throwsIllegalState(() -> cashier.collectPayment(second)), "a second payment in the same turn is refused");
        Customer seated = new RegularCustomer(3, "Cat");
        seated.sitDown();
        check(throwsIllegalState(() -> new Cashier().collectPayment(seated)), "the Cashier cannot charge before READY_TO_PAY");
    }

    private static void waitingAndAbandonment() {
        section("Waiting, abandonment and revenue (Section 3.2 step 5)");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        Customer ann = new RegularCustomer(1, "Ann");   // never seated
        Customer bob = new RegularCustomer(2, "Bob");   // seated and ordered, but never cooked for
        arrivals.put(1, ann);
        arrivals.put(2, bob);
        GameController game = gameWith(arrivals);
        for (int turn = 1; turn <= 14; turn++) {
            game.startTurn();
            if (turn == 2) {
                game.waiterSeat(2);
            } else if (turn == 3) {
                game.waiterTakeOrder(2);
            } else {
                game.waiterWait();
            }
            game.chefWait();
            game.endTurn();
            if (turn == 12) {
                check(ann.getSatisfaction() == 4 && game.getWaitingCustomers().contains(ann),
                        "a queued Regular loses 8 per turn: 4 after 12 turns and still waiting");
            }
            if (turn == 13) {
                check(ann.getSatisfaction() == 0 && ann.getStatus() == CustomerStatus.LEFT && !game.getWaitingCustomers().contains(ann),
                        "at 0 the customer leaves without paying and is removed from the queue");
            }
        }
        check(bob.getSatisfaction() == 0, "satisfaction never drops below 0");
        check(bob.getStatus() == CustomerStatus.LEFT && bob.getOrder().getStatus() == OrderStatus.CANCELLED,
                "a seated customer who leaves has their unfinished order CANCELLED");
        check(game.getTables().get(0).isFree() && game.getTables().get(1).isFree(), "the table of a customer who leaves is freed");
        check(game.getScoreboard().getUnhappyDepartures() == 2, "both departures are counted as unhappy departures");
        check(game.getScoreboard().getRevenue() == 0, "revenue never includes unpaid or abandoned orders");
        game.startTurn();
        game.waiterWait();
        check(!game.chefPrepare(2).isSuccess(), "the Chef cannot work on a cancelled order");
    }

    private static void customersChooseTheirOwnFood() {
        section("Customers choose their own food (documented deterministic rule)");
        Menu menu = GameSetup.createMenu();
        String[] expected = {"Salad", "Burger", "Pasta", "Salad", "Burger", "Pasta"};
        boolean allMatch = true;
        for (int number = 1; number <= 6; number++) {
            MenuItem choice = new RegularCustomer(number, "Customer" + number).chooseMenuItem(menu);
            allMatch = allMatch && choice.getName().equals(expected[number - 1]);
        }
        check(allMatch, "customer n orders menu item n, wrapping around: Salad, Burger, Pasta, Salad, Burger, Pasta");
    }

    private static void lastArrivalCannotPayInTime() {
        section("The customer arriving on turn 16 cannot finish paying within 18 turns");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        Customer late = new RegularCustomer(1, "Lee");   // orders Salad, the quickest dish
        arrivals.put(16, late);
        GameController game = gameWith(arrivals);
        while (!game.isGameOver()) {
            game.startTurn();
            if (game.getTurn() == 16) {
                game.waiterSeat(1);
            } else if (game.getTurn() == 17) {
                game.waiterTakeOrder(1);
                game.chefPrepare(1);
            } else if (game.getTurn() == 18) {
                game.waiterServe(1);
            }
            game.endTurn();
        }
        check(game.getTurn() == 18 && late.getStatus() == CustomerStatus.EATING && game.getScoreboard().getPaidCount() == 0,
                "with perfect play they are still eating after turn 18, so at most five customers can pay");
    }

    private static void objectiveNeedsAllThreeTargets() {
        section("Result: Victory needs all three targets");
        Objective goal = GameSetup.createObjective();
        check(goal.isMetBy(scoreWith(4, 1125, 60)), "exactly 4 paid, $45.00 and average 60 is a VICTORY");
        check(!goal.isMetBy(scoreWith(3, 2000, 90)), "3 paid customers is a DEFEAT");
        check(!goal.isMetBy(scoreWith(4, 1100, 90)), "revenue of $44.00 is a DEFEAT");
        Scoreboard justBelow = scoreWith(3, 1500, 60);
        justBelow.recordPayment(1500, 59);
        check(!goal.isMetBy(justBelow), "average satisfaction 59.75 is a DEFEAT");
        check(!goal.isMetBy(new Scoreboard()) && !new Scoreboard().hasPayments(),
                "with nobody paid there is no average (shown as N/A) and the result is a DEFEAT");
    }

    private static void moneyIsShownWithTwoDecimals() {
        section("Money is stored in cents and shown in dollars with two decimals");
        check(Money.format(900).equals("$9.00") && Money.format(1830).equals("$18.30") && Money.format(5).equals("$0.05"),
                "900 -> $9.00, 1830 -> $18.30, 5 -> $0.05");
    }

    private static void badInputIsHandled() {
        section("Robust input handling through the command-line interface");
        // turn 1: the Waiter gets "hello", 7, then seat #9 (not queued), then seat Alice.
        // Then the input runs out, so everything after that is Wait.
        String answers = "hello\n7\n1\n9\n1\n1\n";
        GameController game = GameSetup.createBaseGame();
        String output = playThroughCli(game, answers, false);
        check(output.contains("\"hello\" is not a number"), "text instead of a number is rejected and asked again");
        check(output.contains("Please enter a number from 0 to 3"), "a number that is not an option is rejected and asked again");
        check(output.contains("Customer #9 is not in the waiting queue"), "an invalid customer is explained and the Waiter chooses again");
        check(output.contains("Waiter seats Alice at Table 1"), "the valid choice is then carried out");
        check(output.contains("(no more input)") && game.isGameOver() && output.contains("RESULT: DEFEAT"),
                "when the input runs out every remaining choice is Wait and the game still plays all 18 turns");
    }

    // replays a saved run through the CLI and compares the output with its transcript
    private static void replay(String runFile, boolean victory, int paid, int revenue, String average) throws IOException {
        String transcriptFile = runFile.replace(".txt", "-transcript.txt");
        section("Replay of " + runFile);
        GameController game = GameSetup.createBaseGame();
        String output = playThroughCli(game, readFile(runFile), true);
        Scoreboard score = game.getScoreboard();
        check(game.isGameOver() && output.contains("TURN 18 of 18"), "all 18 turns are played (no early ending)");
        check(game.isVictory() == victory, "result is " + (victory ? "VICTORY" : "DEFEAT"));
        check(score.getPaidCount() == paid && score.getRevenue() == revenue && oneDecimal(score.getAverageSatisfaction()).equals(average),
                paid + " paid customers, revenue " + Money.format(revenue) + ", average satisfaction " + average);
        check(output.contains("Waiting queue:") && output.contains("Tables:") && output.contains("Staff tasks available:")
                        && output.contains("Average satisfaction of paid customers N/A"),
                "start of turn shows revenue, average (N/A before anyone pays), queue, tables, orders and staff tasks");
        check(output.contains("| Unhappy departures ") && output.contains("| Turns used "),
                "end of turn shows served, unhappy departures, revenue, average satisfaction and turns used");
        check(normalise(output).equals(normalise(readFile(transcriptFile))), "matches the saved transcript " + transcriptFile);
    }

    // base turns, tables, menu and objective but our own arrivals
    private static GameController gameWith(Map<Integer, Customer> arrivals) {
        return new GameController(GameSetup.MAX_TURNS, GameSetup.TABLES, GameSetup.createMenu(), arrivals,
                GameSetup.createObjective());
    }

    // a customer who becomes READY_TO_PAY on the given turn
    private static Customer readyToPayOnTurn(Customer customer, int turn) {
        customer.sitDown();
        Order order = new Order(customer, new MenuItem("Salad", 900, 1), 900);
        customer.placeOrder(order);
        order.prepareOneUnit();
        order.markServed();
        customer.startEating(turn - 1);
        customer.finishEating(turn);
        return customer;
    }

    private static Scoreboard scoreWith(int payments, int amountEach, int satisfactionEach) {
        Scoreboard score = new Scoreboard();
        for (int i = 0; i < payments; i++) {
            score.recordPayment(amountEach, satisfactionEach);
        }
        return score;
    }

    // plays a whole game through the CLI and returns everything it printed
    private static String playThroughCli(GameController game, String answers, boolean echo) {
        PrintStream console = System.out;
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        System.setOut(new PrintStream(printed, true));
        try {
            new RestaurantCLI(game, new Scanner(answers), echo).play();
        } finally {
            System.setOut(console);
        }
        return printed.toString();
    }

    private static boolean isItem(MenuItem item, String name, int price, int prepUnits) {
        return item.getName().equals(name) && item.getPrice() == price && item.getPrepUnits() == prepUnits;
    }

    private static List<String> roles(List<Staff> staff) {
        List<String> roles = new ArrayList<>();
        for (Staff member : staff) {
            roles.add(member.getRole());
        }
        return roles;
    }

    private static boolean throwsIllegalState(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalStateException expected) {
            return true;
        }
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    // ignore Windows line endings
    private static String normalise(String text) {
        return text.replace("\r\n", "\n");
    }

    private static void section(String title) {
        System.out.println();
        System.out.println(title);
    }

    private static void check(boolean condition, String description) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + description);
        } else {
            failed++;
            System.out.println("  FAIL  " + description);
        }
    }
}
