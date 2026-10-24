package restaurant.entity;

// Every customer type goes through the same visit:
// queue -> seated -> ordered -> eating -> ready to pay -> paid (or leaves if satisfaction hits 0).
// The subclasses only change how much satisfaction is lost, the bill and the food choice.
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int number;   // arrival order, #1 arrives first
    private final String name;
    private int satisfaction;
    private CustomerStatus status;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;

    protected Customer(int number, String name) {
        this.number = number;
        this.name = name;
        this.satisfaction = MAX_SATISFACTION;
        this.status = CustomerStatus.WAITING_FOR_TABLE;
    }

    public abstract String getType();

    // lost at the end of every turn spent waiting
    public abstract int getSatisfactionLoss();

    // full price unless a subclass gives a discount
    public int calculateBill(int menuPrice) {
        return menuPrice;
    }

    // food rule: customer n orders item n on the menu, going back to the top when it runs out
    public MenuItem chooseMenuItem(Menu menu) {
        return menu.getItem((number - 1) % menu.size());
    }

    public void sitDown() {
        status = CustomerStatus.SEATED;
    }

    public void placeOrder(Order order) {
        this.order = order;
        status = CustomerStatus.WAITING_FOR_FOOD;
    }

    public void startEating(int turn) {
        status = CustomerStatus.EATING;
        servedTurn = turn;
    }

    // eating takes one full turn, so someone served this turn finishes next turn
    public boolean finishEating(int turn) {
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    // can pay from the turn after becoming READY_TO_PAY
    public boolean canPay(int turn) {
        return status == CustomerStatus.READY_TO_PAY && readyToPayTurn < turn;
    }

    public void pay() {
        if (status != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException(name + " cannot pay before being READY_TO_PAY.");
        }
        status = CustomerStatus.PAID;
        order.markPaid();
    }

    // waiting for a table or for food
    public boolean isWaiting() {
        return status == CustomerStatus.WAITING_FOR_TABLE
                || status == CustomerStatus.SEATED
                || status == CustomerStatus.WAITING_FOR_FOOD;
    }

    public void loseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - getSatisfactionLoss());
    }

    // used by Happy Hour, never goes above 100
    public void recoverSatisfaction(int amount) {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + amount);
    }

    // leaves without paying, an unfinished order gets cancelled
    public void leave() {
        if (!isWaiting()) {
            throw new IllegalStateException(name + " is not waiting, so cannot leave.");
        }
        status = CustomerStatus.LEFT;
        if (order != null) {
            order.cancel();
        }
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    @Override
    public String toString() {
        return "#" + number + " " + name + " (" + getType() + ")";
    }
}
