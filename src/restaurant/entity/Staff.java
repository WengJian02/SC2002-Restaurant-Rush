package restaurant.entity;

// Each staff member does at most one task per turn. Waiting also counts as the task.
public abstract class Staff {

    private boolean actedThisTurn;

    public abstract String getRole();

    // what the role does, printed when the game starts
    public abstract String getDuty();

    public void startTurn() {
        actedThisTurn = false;
    }

    public boolean hasActed() {
        return actedThisTurn;
    }

    public void waitThisTurn() {
        completeTask();
    }

    // every task calls this first, so a second task in the same turn throws
    protected void completeTask() {
        if (actedThisTurn) {
            throw new IllegalStateException("The " + getRole() + " has already done a task this turn.");
        }
        actedThisTurn = true;
    }
}
