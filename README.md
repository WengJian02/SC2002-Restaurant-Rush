# Restaurant Rush

Restaurant Rush is a turn-based restaurant game for the command line, written in Java for our SC2002
group assignment. You play the manager. Each staff member can do one task per turn, so every turn you
decide which customers and orders the Waiter and the Chef deal with first.

This is Stage 1, basic restaurant operation (git tag `stage-1`). Stage 2 (VIP and Critic customers)
and Stage 3 (Host, ComboMeal, Happy Hour) build on it.

Repository: https://github.com/WengJian02/SC2002-Restaurant-Rush

Lab group BACF1, group 5.

## Build and run

You need JDK 8 or newer and nothing else. The game runs offline.

| System | Run the game | Run the tests |
| --- | --- | --- |
| macOS / Linux | `./run.sh` | `./test.sh` |
| Windows | `run.bat` | `test.bat` |

Or by hand, from the project folder:

```
javac -d out -sourcepath src src/restaurant/Main.java
java -cp out restaurant.Main
```

In an IDE (IntelliJ, Eclipse, VS Code), open the project folder, mark `src` as the source root and
run `restaurant.Main`.

Every question is answered with a number. Customers are numbered in order of arrival (#1, #2, ...),
and `0` always means wait or go back.

## Base setting

| Setting | Value |
| --- | --- |
| Turns | 18 (the game always plays all 18) |
| Tables | 2 |
| Staff | Waiter x1 (one task per turn), Chef x1 (one preparation unit per turn), Cashier x1 (one payment per turn, automatic) |
| Arrivals | One customer at the start of turns 1, 4, 7, 10, 13 and 16 |
| Objective | After turn 18: at least 4 paid customers, revenue of at least $45.00 and an average satisfaction of paid customers of at least 60. All three are needed to win. |

| Menu item | Price | Preparation units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

Arrivals: #1 Alice (turn 1), #2 Ben (turn 4), #3 Chloe (turn 7), #4 Daniel (turn 10), #5 Ethan
(turn 13) and #6 Fiona (turn 16). In Stage 1 every customer is a Regular customer, who loses 8
satisfaction for each turn spent waiting and pays the full menu price.

## How a turn works

1. Arrivals. The customer due this turn joins the waiting queue with satisfaction 100.
2. Manager choices. The game shows the turn, revenue, the average satisfaction of paid customers, the
   waiting queue, the tables with each customer's satisfaction and order status, and what each staff
   member could do. Then it asks:
   * what the Waiter does: seat a queued customer, take a seated customer's order, serve a READY
     dish, or wait;
   * which order the Chef works on for one preparation unit (an order taken this turn counts), or
     whether the Chef waits.
3. Kitchen and serving. The Waiter always acts before the Chef, so a dish the Chef finishes this turn
   can only be served next turn.
4. Eating and payment. A customer served on an earlier turn finishes eating and becomes READY_TO_PAY.
   The Cashier charges at most one customer per turn, the one who has been READY_TO_PAY the longest,
   and never on the turn they became READY_TO_PAY. Payment adds the bill to revenue, marks the
   customer and the order PAID and frees the table.
5. Waiting and abandonment. Every customer still waiting for a table or for food loses 8 satisfaction
   (never below 0). A customer who reaches 0 leaves without paying, their unfinished order is
   cancelled and their table is freed.
6. Result. The end of every turn shows served customers, unhappy departures, revenue, average
   satisfaction and turns used. After turn 18 the game shows VICTORY if all three targets are met,
   otherwise DEFEAT.

If a choice is not allowed, for example serving a dish that is still cooking, the game says why and
asks again, and the staff member still has their task. When a staff member has nothing they could do,
the game makes them wait without asking.

## Rules of the game

We kept these rules consistent so that every run can be reproduced. None of them is random.

* Customers choose their own food when the Waiter takes the order. The n-th customer to arrive orders
  the n-th item on the menu, starting again from the top when the menu runs out (Salad, Burger,
  Pasta, Salad, Burger, Pasta). The choice and the bill are printed as soon as the order is placed.
* The manager may seat the customers in the waiting queue in any order, because choosing who comes
  first is what the game is about.
* The Cashier charges the customer who has been READY_TO_PAY the longest.
* Money is stored as whole cents (`int`) and shown with two decimals.
* The game reads its answers from standard input, so a run can be replayed from a file. Text after
  `//` on a line is a note and is ignored. If the input runs out, every remaining choice is "wait" and
  the game still plays to the end.

## Project structure

The code is split into boundary, control and entity classes. The boundary class only reads input
and prints, the control classes run the rules, and the entity classes hold the state and the
behaviour.

```
src/restaurant/
  Main.java                  starts the game with the base setting
  boundary/RestaurantCLI     shows the game and reads the manager's choices (no game rules)
  control/GameController     runs the six turn steps and checks every rule
  control/GameSetup          builds the base setting (menu, tables, arrivals, objective)
  control/ActionResult       outcome of a manager choice, with the reason if refused
  entity/Customer            abstract customer visit: status, satisfaction, order
  entity/RegularCustomer     the Stage 1 customer type
  entity/MenuItem, Menu, Order                   food and orders
  entity/Staff, Waiter, Chef, Cashier            staff (one task per turn each)
  entity/Table, WaitingQueue                     where customers wait
  entity/Scoreboard, Objective                   results and the three targets
  entity/CustomerStatus, OrderStatus             the two status enums
  util/Money                 formats cents as dollars
test/restaurant/RulesTest    rule checks and replays of the scripted runs
runs/stage-1/                scripted victory and defeat runs, with their transcripts
docs/TEST_NOTES.md           test notes for both scripted runs
docs/uml/                    class diagram of the Stage 1 code
```

## Tests and reproducible runs

The game has many small rules that depend on each other across turns. Checking them by playing a
full 18-turn game after every change was slow, and it was easy to miss a rule that had stopped
working, so we wrote the rules down as checks that run in a few seconds.

`./test.sh` (Windows: `test.bat`) runs 78 rule checks, including the brief's Section 3.3 example turn
by turn and what happens when the input is unexpected: a word instead of a number, an option that
does not exist, a customer who is not there, or no input left. It then replays this stage's runs
through the real command-line interface and compares the output with the saved transcripts.

[docs/TEST_NOTES.md](docs/TEST_NOTES.md) goes through the victory run and the defeat run turn by turn.
To watch a run, feed its input file to the game. `--echo` prints each answer next to its prompt, so
the output reads like a live game:

```
./run.sh --echo < runs/stage-1/victory.txt
./run.sh --echo < runs/stage-1/defeat.txt
```

On Windows: `run.bat --echo < runs\stage-1\victory.txt`. The full output of every run is saved next
to it as `*-transcript.txt`.

| Run | Result |
| --- | --- |
| `runs/stage-1/victory.txt` | VICTORY: 5 paid, $66.00, average satisfaction 79.2 |
| `runs/stage-1/defeat.txt` | DEFEAT: 3 paid, $42.00, average satisfaction 65.3 |

## Design documents

[docs/uml/class-diagram.png](docs/uml/class-diagram.png) (and the
[SVG](docs/uml/class-diagram.svg), which stays sharp when zoomed) is the class diagram of the Stage 1
code, in the UML notation from the course's class diagram lecture.

## Stages

| Stage | Adds | Tag |
| --- | --- | --- |
| 1 Basic operation | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings | `stage-1` (this version) |
| 2 Customer behaviour | VIP and Critic customers | `stage-2` |
| 3 New role and menu policy | Host, ComboMeal, Happy Hour | `stage-3` |
