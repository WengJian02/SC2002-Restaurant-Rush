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
import restaurant.entity.ComboMeal;
import restaurant.entity.CriticCustomer;
import restaurant.entity.Customer;
import restaurant.entity.CustomerStatus;
import restaurant.entity.Host;
import restaurant.entity.Menu;
import restaurant.entity.MenuItem;
import restaurant.entity.Objective;
import restaurant.entity.Order;
import restaurant.entity.OrderStatus;
import restaurant.entity.RegularCustomer;
import restaurant.entity.Scoreboard;
import restaurant.entity.Seater;
import restaurant.entity.Staff;
import restaurant.entity.Table;
import restaurant.entity.VIPCustomer;
import restaurant.entity.Waiter;
import restaurant.util.Money;

// Checks the rules from the brief one by one, then replays the saved runs in runs/stage-3
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
        vipAndCriticBehaviour();
        everyCustomerTypeFollowsTheSameFlow();
        controllerHasNoTypeChecks();
        comboMeals();
        hostSeatsOneCustomerPerTurn();
        happyHourPricing();
        happyHourRecovery();
        replay("runs/stage-3/victory.txt", true, 5, 9180, "80.8");
        replay("runs/stage-3/happy-hour-late.txt", true, 5, 8964, "87.4");
        GameController defeat = replay("runs/stage-3/defeat.txt", false, 3, 3870, "65.7");
        check(defeat.getScoreboard().getUnhappyDepartures() == 1,
                "in the defeat run the neglected Critic leaves at 0 and is counted as an unhappy departure");

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
        check(roles(game.getStaff()).equals(Arrays.asList("Host", "Waiter", "Chef", "Cashier")),
                "staff: one Host (Stage 3), one Waiter, one Chef and one Cashier");
        check(game.getArrivalTurns().equals(Arrays.asList(1, 4, 7, 10, 13, 16)),
                "one customer arrives at the start of turns 1, 4, 7, 10, 13 and 16");
        Menu menu = game.getMenu();
        check(isItem(menu.getItem(0), "Salad", 900, 1)
                        && isItem(menu.getItem(1), "Burger", 1500, 1)
                        && isItem(menu.getItem(2), "Pasta", 1800, 2),
                "menu: Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units)");
        check(menu.size() == 6
                        && isItem(menu.getItem(3), "Salad + Burger Combo", 2160, 2)
                        && isItem(menu.getItem(4), "Salad + Pasta Combo", 2430, 3)
                        && isItem(menu.getItem(5), "Burger + Pasta Combo", 2970, 3),
                "Stage 3 menu adds three combos: $21.60 (2 units), $24.30 (3 units), $29.70 (3 units)");
        Objective goal = game.getObjective();
        check(goal.getMinPaidCustomers() == 4 && goal.getMinRevenue() == 4500 && goal.getMinAverageSatisfaction() == 60,
                "objective: at least 4 paid customers, $45.00 revenue and average satisfaction 60");
        List<String> types = new ArrayList<>();
        for (Customer customer : GameSetup.createArrivals().values()) {
            types.add(customer.getType());
        }
        check(types.equals(Arrays.asList("Regular", "VIP", "Regular", "Critic", "Regular", "Regular")),
                "still six arrivals: a VIP (turn 4) and a Critic (turn 10) replace two Regular customers");
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
        Menu menu = threeDishMenu();
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
        // turn 1: Happy Hour gets "hello", 7, then 0 (no). Host gets 9 (not queued), then 0 (wait).
        // Waiter gets 7, then seats Alice. Then the input runs out, so everything after that is Wait.
        String answers = "hello\n7\n0\n9\n0\n7\n1\n1\n";
        GameController game = GameSetup.createBaseGame();
        String output = playThroughCli(game, answers, false);
        check(output.contains("\"hello\" is not a number"), "text instead of a number is rejected and asked again");
        check(output.contains("Please enter a number from 0 to 3"), "a number that is not an option is rejected and asked again");
        check(output.contains("Customer #9 is not in the waiting queue"), "an invalid customer is explained and the Host chooses again");
        check(output.contains("Waiter seats Alice at Table 1"), "the valid choice is then carried out");
        check(output.contains("(no more input)") && game.isGameOver() && output.contains("RESULT: DEFEAT"),
                "when the input runs out every remaining choice is Wait and the game still plays all 18 turns");
    }

    private static void vipAndCriticBehaviour() {
        section("Stage 2: VIP and Critic customers");
        Customer regular = new RegularCustomer(1, "Rita");
        Customer vip = new VIPCustomer(2, "Victor");
        Customer critic = new CriticCustomer(3, "Carmen");
        regular.loseSatisfaction();
        vip.loseSatisfaction();
        critic.loseSatisfaction();
        check(regular.getSatisfaction() == 92 && vip.getSatisfaction() == 95 && critic.getSatisfaction() == 88,
                "satisfaction lost per waiting turn: Regular 8, VIP 5, Critic 12");
        check(vip.calculateBill(1500) == 1350 && vip.calculateBill(900) == 810 && vip.calculateBill(1800) == 1620,
                "VIP bill is 10% off: $15.00 -> $13.50, $9.00 -> $8.10, $18.00 -> $16.20");
        check(regular.calculateBill(1500) == 1500 && critic.calculateBill(1500) == 1500, "Regular and Critic pay the full price");
        check(Money.percentOf(999, 90) == 899 && Money.percentOf(1005, 90) == 905,
                "percentages are rounded to the nearest cent ($8.991 -> $8.99, $9.045 -> $9.05)");

        Menu menu = threeDishMenu();
        boolean alwaysPasta = true;
        for (int number = 1; number <= 6; number++) {
            alwaysPasta = alwaysPasta && new CriticCustomer(number, "Critic" + number).chooseMenuItem(menu).getName().equals("Pasta");
        }
        check(alwaysPasta, "a Critic always orders the most demanding dish (Pasta, 2 units), whatever their arrival number");
        Menu tied = new Menu();
        tied.addItem(new MenuItem("Stew", 1200, 2));
        tied.addItem(new MenuItem("Roast", 2500, 2));
        tied.addItem(new MenuItem("Soup", 700, 1));
        check(critic.chooseMenuItem(tied).getName().equals("Roast"), "if two dishes need the same work, the Critic picks the more expensive one");
    }

    private static void everyCustomerTypeFollowsTheSameFlow() {
        section("Stage 2: every customer type follows the same order and service flow");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        Customer vip = new VIPCustomer(2, "Victor");   // orders Burger under the food rule
        Customer critic = new CriticCustomer(1, "Carmen"); // would order Salad, but as a Critic orders Pasta
        arrivals.put(1, vip);
        arrivals.put(2, critic);
        GameController game = gameWith(arrivals);
        game.startTurn();   // turn 1: seat Victor
        game.waiterSeat(2);
        game.endTurn();
        game.startTurn();   // turn 2: Carmen arrives; take Victor's order and cook it
        ActionResult order = game.waiterTakeOrder(2);
        game.chefPrepare(2);
        game.endTurn();
        check(order.getMessage().contains("Burger") && order.getMessage().contains("$13.50"),
                "the VIP's Burger is billed $13.50 when the order is taken");
        check(vip.getSatisfaction() == 90 && critic.getSatisfaction() == 88,
                "after waiting, the VIP lost 5 per turn (90) and the Critic 12 (88)");
        game.startTurn();   // turn 3: serve Victor
        game.waiterServe(2);
        game.endTurn();
        game.startTurn();   // turn 4: seat Carmen; Victor finishes eating
        game.waiterSeat(1);
        game.endTurn();
        game.startTurn();   // turn 5: Carmen orders; Victor pays
        ActionResult criticOrder = game.waiterTakeOrder(1);
        game.chefPrepare(1);
        game.endTurn();
        check(criticOrder.getMessage().contains("Pasta"), "the Critic's order goes through the same Waiter and Chef steps");
        check(vip.getStatus() == CustomerStatus.PAID && game.getScoreboard().getRevenue() == 1350,
                "the VIP becomes READY_TO_PAY and pays $13.50 like everyone else");
        for (int turn = 6; turn <= 9; turn++) {
            game.startTurn();
            if (turn == 6) {
                game.waiterWait();
                game.chefPrepare(1);
            } else if (turn == 7) {
                game.waiterServe(1);
            }
            game.endTurn();
        }
        check(critic.getStatus() == CustomerStatus.PAID && game.getScoreboard().getRevenue() == 1350 + 1800,
                "the Critic is served, eats and pays $18.00 through the same steps");
    }

    private static void controllerHasNoTypeChecks() {
        section("Stage 2: behaviour is dispatched through the customer objects");
        String controller;
        try {
            controller = readFile("src/restaurant/control/GameController.java");
        } catch (IOException e) {
            check(false, "GameController.java can be read (run the tests from the project folder)");
            return;
        }
        check(!controller.contains("instanceof") && !controller.contains("VIPCustomer") && !controller.contains("CriticCustomer")
                        && !controller.contains("getType()"),
                "GameController has no instanceof or type checks: it only calls Customer methods");
    }

    private static void comboMeals() {
        section("Stage 3: ComboMeal");
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        ComboMeal saladPasta = new ComboMeal(salad, pasta);
        check(isItem(saladPasta, "Salad + Pasta Combo", 2430, 3)
                        && saladPasta.getFirst() == salad && saladPasta.getSecond() == pasta,
                "brief's example: Salad + Pasta = ($9 + $18) x 0.9 = $24.30, 3 units, made of those two items");
        check(isItem(new ComboMeal(salad, burger), "Salad + Burger Combo", 2160, 2)
                        && isItem(new ComboMeal(burger, pasta), "Burger + Pasta Combo", 2970, 3),
                "Salad + Burger = $21.60 (2 units), Burger + Pasta = $29.70 (3 units)");
        check(new ComboMeal(new MenuItem("A", 999, 1), new MenuItem("B", 500, 1)).getPrice() == 1349
                        && new ComboMeal(new MenuItem("A", 505, 1), new MenuItem("B", 500, 1)).getPrice() == 905,
                "the combo price is rounded to the nearest cent ($13.491 -> $13.49, $9.045 -> $9.05)");

        Menu menu = GameSetup.createMenu();
        String[] expected = {"Salad", "Burger", "Pasta", "Salad + Burger Combo", "Salad + Pasta Combo", "Burger + Pasta Combo"};
        boolean allMatch = true;
        for (int number = 1; number <= 6; number++) {
            allMatch = allMatch && new RegularCustomer(number, "Customer" + number).chooseMenuItem(menu).getName().equals(expected[number - 1]);
        }
        check(allMatch, "combo choice rule: the same food rule on the longer menu, so customers 4, 5 and 6 choose combos");
        check(new CriticCustomer(4, "Carmen").chooseMenuItem(menu).getName().equals("Burger + Pasta Combo"),
                "a Critic chooses the Burger + Pasta Combo: 3 units, and dearer than the other 3-unit combo");

        Map<Integer, Customer> arrivals = new TreeMap<>();
        Customer eve = new RegularCustomer(5, "Eve");   // customer #5 orders the Salad + Pasta Combo
        arrivals.put(1, eve);
        GameController game = gameWith(arrivals, menu);
        game.startTurn();
        game.hostSeat(5);
        ActionResult order = game.waiterTakeOrder(5);
        game.chefPrepare(5);
        game.endTurn();
        check(order.getMessage().contains("Salad + Pasta Combo") && order.getMessage().contains("$24.30"),
                "a customer orders a combo like any dish and its price is shown");
        for (int turn = 2; turn <= 3; turn++) {
            game.startTurn();
            game.waiterWait();
            game.chefPrepare(5);
            game.endTurn();
        }
        check(eve.getOrder().isReady() && eve.getOrder().getUnitsDone() == 3, "the Chef prepares a combo one unit per turn: READY after 3 units");
        while (!game.isGameOver()) {
            game.startTurn();
            game.waiterServe(5);
            game.endTurn();
        }
        check(eve.getStatus() == CustomerStatus.PAID && game.getScoreboard().getRevenue() == 2430,
                "the combo is served, eaten and paid through the usual steps ($24.30)");
    }

    private static void hostSeatsOneCustomerPerTurn() {
        section("Stage 3: Host");
        check(new Host() instanceof Seater && new Waiter() instanceof Seater, "both the Host and the Waiter can seat (Seater)");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        arrivals.put(1, new RegularCustomer(1, "Ann"));
        arrivals.put(2, new RegularCustomer(2, "Bob"));
        arrivals.put(3, new RegularCustomer(3, "Cat"));
        GameController game = gameWith(arrivals);
        game.startTurn();
        check(game.hostSeat(1).isSuccess() && game.waiterTakeOrder(1).isSuccess(),
                "a customer seated by the Host can give their order to the Waiter in the same turn");
        game.endTurn();

        game.startTurn();
        check(game.hostSeat(2).isSuccess(), "the Host seats a queued customer");
        game.endTurn();
        game.startTurn();
        ActionResult noTable = game.hostSeat(3);
        check(!noTable.isSuccess() && noTable.getMessage().contains("Every table is occupied"),
                "the Host follows the same seating rule as the Waiter (refused when no table is free)");

        Map<Integer, Customer> twoWaiting = new TreeMap<>();
        twoWaiting.put(1, new RegularCustomer(1, "Ann"));
        twoWaiting.put(2, new RegularCustomer(2, "Bob"));
        GameController busy = gameWith(twoWaiting);
        busy.startTurn();
        busy.hostWait();
        busy.waiterWait();
        busy.endTurn();
        busy.startTurn();
        check(busy.hostSeat(1).isSuccess(), "turn 2: the Host seats Ann");
        ActionResult second = busy.hostSeat(2);
        check(!second.isSuccess() && second.getMessage().contains("Host has already done a task"),
                "the Host seats at most one customer per turn");
        check(busy.waiterSeat(2).isSuccess(), "the Waiter can still seat Bob in the same turn, in addition to the Host");
        check(!busy.hostHasTask() && busy.getWaitingCustomers().isEmpty(), "both customers were seated in one turn");
    }

    private static void happyHourPricing() {
        section("Stage 3: Happy Hour - activation, duration and pricing");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        Customer victor = new VIPCustomer(2, "Victor");   // orders Burger
        Customer ann = new RegularCustomer(1, "Ann");   // orders Salad
        Customer cat = new RegularCustomer(3, "Cat");   // orders Pasta
        arrivals.put(1, victor);
        arrivals.put(2, ann);
        arrivals.put(3, cat);
        GameController game = gameWith(arrivals);

        game.startTurn();   // turn 1: Victor orders before Happy Hour
        game.hostSeat(2);
        ActionResult before = game.waiterTakeOrder(2);
        game.chefPrepare(2);
        ActionResult late = game.activateHappyHour();
        check(before.getMessage().contains("$13.50"), "before Happy Hour the VIP's Burger costs $13.50");
        check(!late.isSuccess() && late.getMessage().contains("start of a turn"),
                "Happy Hour cannot be switched on after a staff member has acted this turn");
        game.endTurn();

        game.startTurn();   // turn 2: switch Happy Hour on
        check(game.canActivateHappyHour() && game.activateHappyHour().isSuccess(), "Happy Hour is switched on at the start of turn 2");
        check(game.isHappyHourActive() && game.getHappyHourTurnsLeft() == 2,
                "it lasts this turn and the next (2 turns left when switched on)");
        game.hostSeat(1);
        ActionResult during = game.waiterTakeOrder(1);
        game.chefPrepare(1);
        game.endTurn();
        check(during.getMessage().contains("$7.20"), "an order taken during Happy Hour gets 20% off ($9.00 -> $7.20)");
        check(victor.getOrder().getBill() == 1350, "an order placed before Happy Hour keeps its locked price");

        game.startTurn();   // turn 3: second and last Happy Hour turn
        check(game.isHappyHourActive() && game.getHappyHourTurnsLeft() == 1, "turn 3: still on, 1 turn left");
        ActionResult again = game.activateHappyHour();
        check(!again.isSuccess() && again.getMessage().contains("already been used"), "Happy Hour can be used only once per game");
        game.waiterServe(2);
        game.endTurn();
        check(!game.isHappyHourActive() && !game.isHappyHourAvailable(), "after its two turns Happy Hour is over and cannot return");

        while (game.getTurn() < 6) {   // turns 4-6: Cat orders after Happy Hour
            game.startTurn();
            if (game.getTurn() == 4) {
                game.waiterServe(1);
            } else if (game.getTurn() == 6) {
                game.hostSeat(3);
                ActionResult after = game.waiterTakeOrder(3);
                check(after.getMessage().contains("Bill: $18.00."), "an order taken after Happy Hour pays the full price");
            }
            game.endTurn();
        }
        check(game.getScoreboard().getRevenue() == 1350 + 720, "the locked prices are what the Cashier collects ($13.50 + $7.20)");

        Map<Integer, Customer> vipOnly = new TreeMap<>();
        vipOnly.put(1, new VIPCustomer(2, "Vera"));
        GameController stacking = gameWith(vipOnly);
        stacking.startTurn();
        stacking.activateHappyHour();
        stacking.hostSeat(2);
        check(stacking.waiterTakeOrder(2).getMessage().contains("$10.80"),
                "VIP discount first, then Happy Hour: Burger $15.00 x 0.9 x 0.8 = $10.80 (brief's example)");
    }

    private static void happyHourRecovery() {
        section("Stage 3: Happy Hour - satisfaction recovery");
        Map<Integer, Customer> arrivals = new TreeMap<>();
        Customer rita = new RegularCustomer(1, "Rita");    // served before Happy Hour
        Customer carl = new CriticCustomer(4, "Carl");   // left waiting in the queue
        arrivals.put(1, rita);
        arrivals.put(2, carl);
        GameController game = gameWith(arrivals);
        game.startTurn();   // turn 1: Rita seated, ordered and cooked for
        game.hostSeat(1);
        game.waiterTakeOrder(1);
        game.chefPrepare(1);
        game.endTurn();
        game.startTurn();   // turn 2: Rita served; Carl arrives and waits
        game.hostWait();
        game.waiterServe(1);
        game.endTurn();
        game.startTurn();   // turn 3: nobody acts
        game.hostWait();
        game.waiterWait();
        game.endTurn();
        game.startTurn();   // turn 4: Happy Hour on
        game.activateHappyHour();
        check(carl.getSatisfaction() == 91, "a customer waiting for a table recovers 15 as soon as Happy Hour starts (76 -> 91)");
        check(rita.getStatus() == CustomerStatus.READY_TO_PAY && rita.getSatisfaction() == 92,
                "a customer who has already been served does not recover (stays 92)");
        game.hostWait();
        game.waiterWait();
        game.endTurn();
        game.startTurn();   // turn 5: second Happy Hour turn
        check(carl.getSatisfaction() == 94, "at the start of the second turn the waiting customer recovers 15 again (79 -> 94)");

        Map<Integer, Customer> leaver = new TreeMap<>();
        Customer critic = new CriticCustomer(4, "Cora");
        leaver.put(1, critic);
        GameController late = gameWith(leaver);
        for (int turn = 1; turn <= 9; turn++) {
            late.startTurn();
            late.hostWait();
            late.waiterWait();
            late.endTurn();
        }
        late.startTurn();
        late.activateHappyHour();
        check(critic.getStatus() == CustomerStatus.LEFT && critic.getSatisfaction() == 0,
                "a customer who has already left is not affected by Happy Hour");

        Customer capped = new RegularCustomer(1, "Max");
        capped.loseSatisfaction();
        capped.recoverSatisfaction(15);
        check(capped.getSatisfaction() == 100, "recovery never goes above 100 (92 + 15 -> 100)");
    }

    // replays a saved run through the CLI and compares the output with its transcript
    private static GameController replay(String runFile, boolean victory, int paid, int revenue, String average) throws IOException {
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
        return game;
    }

    // base turns, tables and objective but our own arrivals. It uses the brief's three dishes, so the
    // stage 1 and 2 checks still get the same orders now that the menu also has combos.
    private static GameController gameWith(Map<Integer, Customer> arrivals) {
        return gameWith(arrivals, threeDishMenu());
    }

    private static GameController gameWith(Map<Integer, Customer> arrivals, Menu menu) {
        return new GameController(GameSetup.MAX_TURNS, GameSetup.TABLES, menu, arrivals, GameSetup.createObjective());
    }

    private static Menu threeDishMenu() {
        Menu menu = new Menu();
        menu.addItem(new MenuItem("Salad", 900, 1));
        menu.addItem(new MenuItem("Burger", 1500, 1));
        menu.addItem(new MenuItem("Pasta", 1800, 2));
        return menu;
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
