package restaurant.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import restaurant.entity.Cashier;
import restaurant.entity.Chef;
import restaurant.entity.Customer;
import restaurant.entity.CustomerStatus;
import restaurant.entity.HappyHour;
import restaurant.entity.Host;
import restaurant.entity.Menu;
import restaurant.entity.MenuItem;
import restaurant.entity.Objective;
import restaurant.entity.Order;
import restaurant.entity.Scoreboard;
import restaurant.entity.Seater;
import restaurant.entity.Staff;
import restaurant.entity.Table;
import restaurant.entity.WaitingQueue;
import restaurant.entity.Waiter;
import restaurant.util.Money;

// Runs the turns and checks the rules. The CLI passes in the manager's choices
// and prints the messages that come back.
public class GameController {

    private final int maxTurns;
    private final Menu menu;
    private final Objective objective;
    private final Map<Integer, Customer> arrivals;   // turn -> customer who arrives that turn
    private final List<Customer> customers = new ArrayList<>();   // everyone who has arrived so far
    private final List<Table> tables = new ArrayList<>();
    private final WaitingQueue queue = new WaitingQueue();
    private final Host host = new Host();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final HappyHour happyHour = new HappyHour();
    private final Scoreboard scoreboard = new Scoreboard();
    private int turn = 0;

    public GameController(int maxTurns, int tableCount, Menu menu, Map<Integer, Customer> arrivals,
                          Objective objective) {
        this.maxTurns = maxTurns;
        this.menu = menu;
        this.arrivals = arrivals;
        this.objective = objective;
        for (int number = 1; number <= tableCount; number++) {
            tables.add(new Table(number));
        }
    }

    // Step 1: start the next turn, the customer due this turn joins the queue
    public List<String> startTurn() {
        turn++;
        for (Staff member : getStaff()) {
            member.startTurn();
        }
        List<String> events = new ArrayList<>();
        Customer arriving = arrivals.get(turn);
        if (arriving != null) {
            customers.add(arriving);
            queue.add(arriving);
            events.add(arriving + " arrives and joins the waiting queue with satisfaction "
                    + arriving.getSatisfaction() + ".");
        }
        // still on from last turn, so this is its second turn
        if (happyHour.isActive()) {
            events.add("Second turn of Happy Hour. " + recoverWaitingCustomers());
        }
        return events;
    }

    // Steps 2 and 3: the manager's choices.
    // When a choice is refused we say why, and the staff member has not used up their task.

    // only once per game, and only before any staff member has acted this turn
    public ActionResult activateHappyHour() {
        if (!happyHour.isAvailable()) {
            return ActionResult.failure("Happy Hour has already been used in this game.");
        }
        if (anyStaffHasActed()) {
            return ActionResult.failure("Happy Hour can only be switched on at the start of a turn, "
                    + "before any staff member acts.");
        }
        happyHour.activate();
        return ActionResult.success("Happy Hour is ON for this turn and the next: orders taken get "
                + HappyHour.DISCOUNT_PERCENT + "% off. " + recoverWaitingCustomers());
    }

    public ActionResult hostSeat(int customerNumber) {
        return seatFromQueue(host, customerNumber);
    }

    public ActionResult hostWait() {
        if (host.hasActed()) {
            return alreadyActed(host.getRole());
        }
        host.waitThisTurn();
        return ActionResult.success("The Host waits this turn.");
    }

    public ActionResult waiterSeat(int customerNumber) {
        return seatFromQueue(waiter, customerNumber);
    }

    public ActionResult waiterTakeOrder(int customerNumber) {
        if (waiter.hasActed()) {
            return alreadyActed(waiter.getRole());
        }
        Customer customer = findCustomer(customerNumber);
        if (customer == null) {
            return notArrived(customerNumber);
        }
        if (customer.getStatus() != CustomerStatus.SEATED) {
            return ActionResult.failure("Only a seated customer who has not ordered yet can order. "
                    + customer.getName() + " is " + customer.getStatus() + ".");
        }
        // the customer picks the food. Their own discount first, then Happy Hour.
        // The bill is locked into the order from now on.
        MenuItem item = customer.chooseMenuItem(menu);
        int bill = customer.calculateBill(item.getPrice());
        bill = happyHour.applyDiscount(bill);
        waiter.takeOrder(customer, item, bill);

        String message = customer.getName() + " orders " + item.getName() + " ("
                + item.getPrepUnitsText() + "). Bill: " + Money.format(bill);
        if (bill != item.getPrice()) {
            message += " (menu price " + Money.format(item.getPrice());
            if (happyHour.isActive()) {
                message += ", Happy Hour " + HappyHour.DISCOUNT_PERCENT + "% off included";
            }
            message += ")";
        }
        return ActionResult.success(message + ".");
    }

    public ActionResult waiterServe(int customerNumber) {
        if (waiter.hasActed()) {
            return alreadyActed(waiter.getRole());
        }
        Customer customer = findCustomer(customerNumber);
        if (customer == null) {
            return notArrived(customerNumber);
        }
        Order order = customer.getOrder();
        if (order == null) {
            return ActionResult.failure(customer.getName() + " has not ordered yet, so there is nothing to serve.");
        }
        String dish = customer.getName() + "'s " + order.getItem().getName();
        if (order.isPreparing()) {
            return ActionResult.failure(dish + " is not READY yet (" + order.getUnitsDone() + " of "
                    + order.getItem().getPrepUnitsText() + " done).");
        }
        if (!order.isReady()) {
            return ActionResult.failure(dish + " is " + order.getStatus() + ", so there is nothing to serve.");
        }
        // the Waiter acts before the Chef, so food that became READY this turn waits until next turn
        waiter.serve(order, turn);
        scoreboard.recordServed();
        return ActionResult.success("Waiter serves " + dish + ": SERVED. " + customer.getName() + " starts eating.");
    }

    public ActionResult waiterWait() {
        if (waiter.hasActed()) {
            return alreadyActed(waiter.getRole());
        }
        waiter.waitThisTurn();
        return ActionResult.success("The Waiter waits this turn.");
    }

    public ActionResult chefPrepare(int customerNumber) {
        if (chef.hasActed()) {
            return alreadyActed(chef.getRole());
        }
        if (!waiter.hasActed()) {
            return ActionResult.failure("The Waiter acts before the Chef. Choose the Waiter's task first.");
        }
        Customer customer = findCustomer(customerNumber);
        if (customer == null) {
            return notArrived(customerNumber);
        }
        Order order = customer.getOrder();
        if (order == null) {
            return ActionResult.failure(customer.getName() + " has not ordered yet, so there is nothing to prepare.");
        }
        String dish = customer.getName() + "'s " + order.getItem().getName();
        if (!order.isPreparing()) {
            return ActionResult.failure(dish + " is " + order.getStatus() + ", so it needs no more preparation.");
        }
        chef.prepare(order);
        String message = "Chef prepares " + dish + ": unit " + order.getUnitsDone() + " of "
                + order.getItem().getPrepUnits() + " done.";
        if (order.isReady()) {
            message += " It is READY and can be served from next turn.";
        }
        return ActionResult.success(message);
    }

    public ActionResult chefWait() {
        if (chef.hasActed()) {
            return alreadyActed(chef.getRole());
        }
        chef.waitThisTurn();
        return ActionResult.success("The Chef waits this turn.");
    }

    // Steps 4 and 5, at the end of the turn
    public List<String> endTurn() {
        List<String> events = new ArrayList<>();
        checkFinishedEating(events);
        chargeNextCustomer(events);
        checkWaitingCustomers(events);
        updateHappyHour(events);
        return events;
    }

    // Step 6: there is no early ending, the game always plays every turn
    public boolean isGameOver() {
        return turn >= maxTurns;
    }

    public boolean isVictory() {
        return objective.isMetBy(scoreboard);
    }

    // getters used by the CLI to show the game

    public int getTurn() {
        return turn;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public Menu getMenu() {
        return menu;
    }

    public Objective getObjective() {
        return objective;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public List<Staff> getStaff() {
        return Arrays.asList(host, waiter, chef, cashier);
    }

    public List<Integer> getArrivalTurns() {
        return new ArrayList<>(arrivals.keySet());
    }

    public List<Customer> getWaitingCustomers() {
        return queue.getCustomers();
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<Customer> getCustomersToSeat() {
        if (findFreeTable() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(queue.getCustomers());
    }

    public List<Customer> getCustomersToOrder() {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getStatus() == CustomerStatus.SEATED) {
                result.add(customer);
            }
        }
        return result;
    }

    public List<Customer> getCustomersToServe() {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : customers) {
            Order order = customer.getOrder();
            if (order != null && order.isReady()) {
                result.add(customer);
            }
        }
        return result;
    }

    public List<Customer> getCustomersToCookFor() {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : customers) {
            Order order = customer.getOrder();
            if (order != null && order.isPreparing()) {
                result.add(customer);
            }
        }
        return result;
    }

    public Customer getNextCustomerToPay() {
        return cashier.chooseNextCustomer(customers, turn);
    }

    public boolean hostHasTask() {
        return !host.hasActed() && !getCustomersToSeat().isEmpty();
    }

    public boolean canActivateHappyHour() {
        return happyHour.isAvailable() && !anyStaffHasActed();
    }

    public boolean isHappyHourAvailable() {
        return happyHour.isAvailable();
    }

    public boolean isHappyHourActive() {
        return happyHour.isActive();
    }

    public int getHappyHourTurnsLeft() {
        return happyHour.getTurnsLeft();
    }

    public boolean waiterHasTask() {
        if (waiter.hasActed()) {
            return false;
        }
        return !getCustomersToSeat().isEmpty() || !getCustomersToOrder().isEmpty()
                || !getCustomersToServe().isEmpty();
    }

    public boolean chefHasTask() {
        return !chef.hasActed() && !getCustomersToCookFor().isEmpty();
    }

    // Step 4: anyone served on an earlier turn has finished eating
    private void checkFinishedEating(List<String> events) {
        for (Customer customer : customers) {
            if (customer.finishEating(turn)) {
                events.add(customer.getName()
                        + " has finished eating and is READY_TO_PAY (pays next turn at the earliest).");
            }
        }
    }

    // Step 4: the Cashier charges at most one customer per turn
    private void chargeNextCustomer(List<String> events) {
        Customer payer = cashier.chooseNextCustomer(customers, turn);
        if (payer == null) {
            return;
        }
        int amount = cashier.collectPayment(payer);
        Table table = freeTableOf(payer);
        scoreboard.recordPayment(amount, payer.getSatisfaction());
        String message = "Cashier collects " + Money.format(amount) + " from " + payer.getName()
                + ": PAID with satisfaction " + payer.getSatisfaction() + " recorded.";
        if (table != null) {
            message += " Table " + table.getNumber() + " is free again.";
        }
        events.add(message);
    }

    // Step 5: everyone still waiting loses satisfaction, and anyone who reaches 0 leaves
    private void checkWaitingCustomers(List<String> events) {
        List<String> losses = new ArrayList<>();
        List<Customer> leaving = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.isWaiting()) {
                customer.loseSatisfaction();
                losses.add(customer.getName() + " " + customer.getSatisfaction());
                if (customer.getSatisfaction() == 0) {
                    leaving.add(customer);
                }
            }
        }
        if (!losses.isEmpty()) {
            events.add("Still waiting, so satisfaction drops: " + String.join(", ", losses) + ".");
        }

        for (Customer customer : leaving) {
            customer.leave();
            queue.remove(customer);
            Table table = freeTableOf(customer);
            scoreboard.recordUnhappyDeparture();
            String message = customer.getName() + " reached 0 satisfaction and leaves without paying.";
            if (customer.getOrder() != null) {
                message += " Their " + customer.getOrder().getItem().getName() + " order is CANCELLED.";
            }
            if (table != null) {
                message += " Table " + table.getNumber() + " is free again.";
            }
            events.add(message);
        }
    }

    // Happy Hour uses up one of its turns at the end of each turn it is on
    private void updateHappyHour(List<String> events) {
        if (!happyHour.isActive()) {
            return;
        }
        happyHour.endTurn();
        if (happyHour.isActive()) {
            int left = happyHour.getTurnsLeft();
            events.add("Happy Hour continues next turn (" + left + (left == 1 ? " turn" : " turns") + " left).");
        } else {
            events.add("Happy Hour is over.");
        }
    }

    // seating rule for both the Waiter and the Host
    private ActionResult seatFromQueue(Seater seater, int customerNumber) {
        if (seater.hasActed()) {
            return alreadyActed(seater.getRole());
        }
        Customer customer = findCustomer(customerNumber);
        if (customer == null || !queue.contains(customer)) {
            return ActionResult.failure("Customer #" + customerNumber + " is not in the waiting queue.");
        }
        Table table = findFreeTable();
        if (table == null) {
            return ActionResult.failure("Every table is occupied. "
                    + "A table is only freed when its customer pays or leaves.");
        }
        seater.seat(customer, table);
        queue.remove(customer);
        return ActionResult.success(seater.getRole() + " seats " + customer.getName()
                + " at Table " + table.getNumber() + ".");
    }

    // Happy Hour: customers waiting for a table or for food get some satisfaction back (max 100)
    private String recoverWaitingCustomers() {
        List<String> recovered = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.isWaiting()) {
                customer.recoverSatisfaction(HappyHour.RECOVERY);
                recovered.add(customer.getName() + " " + customer.getSatisfaction());
            }
        }
        if (recovered.isEmpty()) {
            return "Nobody is waiting, so nobody recovers satisfaction.";
        }
        return "Customers waiting for a table or for food recover " + HappyHour.RECOVERY
                + " satisfaction (max " + Customer.MAX_SATISFACTION + "): " + String.join(", ", recovered) + ".";
    }

    private boolean anyStaffHasActed() {
        for (Staff member : getStaff()) {
            if (member.hasActed()) {
                return true;
            }
        }
        return false;
    }

    private Customer findCustomer(int number) {
        for (Customer customer : customers) {
            if (customer.getNumber() == number) {
                return customer;
            }
        }
        return null;
    }

    private Table findFreeTable() {
        for (Table table : tables) {
            if (table.isFree()) {
                return table;
            }
        }
        return null;
    }

    // frees the table of a customer who paid or left (null if they never got a table)
    private Table freeTableOf(Customer customer) {
        for (Table table : tables) {
            if (table.getCustomer() == customer) {
                table.free();
                return table;
            }
        }
        return null;
    }

    private ActionResult alreadyActed(String role) {
        return ActionResult.failure("The " + role + " has already done a task this turn.");
    }

    private ActionResult notArrived(int customerNumber) {
        return ActionResult.failure("Customer #" + customerNumber + " has not arrived.");
    }
}
