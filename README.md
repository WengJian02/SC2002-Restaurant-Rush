# Restaurant Rush

Restaurant Rush is a turn-based restaurant game for the command line, written in Java for our SC2002
group assignment. You play the manager. Each staff member can do one task per turn, so every turn you
decide which customers and orders the Waiter and the Chef deal with first.

This is Stage 2, customer behaviour (git tag `stage-2`). It adds VIP and Critic customers to the
Stage 1 game (tag `stage-1`), and Stage 3 (Host, ComboMeal, Happy Hour) builds on it. What changed is
listed under [Stages and changes](#stages-and-changes).

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

## Customers

| Type | Satisfaction lost per waiting turn | Bill | Special behaviour |
| --- | --- | --- | --- |
| Regular | 8 | Full price | none |
| VIP | 5 | 10% off | none |
| Critic | 12 | Full price | Always orders the most demanding dish (see below) |

Arrivals in the base setting: #1 Alice (Regular, turn 1), #2 Victor (VIP, turn 4), #3 Chloe (Regular,
turn 7), #4 Carmen (Critic, turn 10), #5 Ethan (Regular, turn 13) and #6 Fiona (Regular, turn 16). The
VIP and the Critic take the place of two Stage 1 Regulars, so there are still six arrivals. Every type
goes through the same visit (seated, orders, eats, pays). The customer classes decide what differs,
and the turn controller never checks a customer's type.

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
5. Waiting and abandonment. Every customer still waiting for a table or for food loses satisfaction
   (8 for Regular, 5 for VIP, 12 for Critic, never below 0). A customer who reaches 0 leaves without
   paying, their unfinished order is cancelled and their table is freed.
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
* A Critic ignores the food rule and orders the most demanding dish on the menu, to test the kitchen:
  the dish with the most preparation units, and the more expensive one if two are tied. On this menu
  that is Pasta (customer #4 would otherwise get Salad). Since a Critic also loses 12 per waiting turn,
  slow service costs the most with a Critic.
* The VIP discount is 10% off the menu price, rounded to the nearest cent ($15.00 becomes $13.50).
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
  entity/RegularCustomer, VIPCustomer, CriticCustomer   the three customer types
  entity/MenuItem, Menu, Order                          food and orders
  entity/Staff, Waiter, Chef, Cashier                   staff (one task per turn each)
  entity/Table, WaitingQueue                            where customers wait
  entity/Scoreboard, Objective                          results and the three targets
  entity/CustomerStatus, OrderStatus                    the two status enums
  util/Money                 formats cents as dollars and works out percentages
test/restaurant/RulesTest    rule checks and replays of the scripted runs
runs/stage-N/                scripted runs for each stage, with their transcripts
docs/TEST_NOTES.md           test notes for every scripted run
docs/uml/                    class diagram of the Stage 2 code
```

## Tests and reproducible runs

The game has many small rules that depend on each other across turns. Checking them by playing a
full 18-turn game after every change was slow, and it was easy to miss a rule that had stopped
working, so we wrote the rules down as checks that run in a few seconds. When we added Stage 2, the
Stage 1 checks showed straight away whether the rules we already had still held.

`./test.sh` (Windows: `test.bat`) runs 97 rule checks, including the brief's Section 3.3 example turn
by turn and what happens when the input is unexpected: a word instead of a number, an option that
does not exist, a customer who is not there, or no input left. It then replays this stage's runs
through the real command-line interface and compares the output with the saved transcripts.

[docs/TEST_NOTES.md](docs/TEST_NOTES.md) goes through one victory run and one defeat run for every
stage, turn by turn. To watch a run, feed its input file to the game. `--echo` prints each answer next
to its prompt, so the output reads like a live game:

```
./run.sh --echo < runs/stage-2/victory.txt
./run.sh --echo < runs/stage-2/defeat.txt
```

On Windows: `run.bat --echo < runs\stage-2\victory.txt`. Each stage's runs belong to that stage's
arrivals, so replay `runs/stage-1` after `git checkout stage-1`. The full output of every run is saved
next to it as `*-transcript.txt`.

| Run | Result |
| --- | --- |
| `runs/stage-1/victory.txt` | VICTORY: 5 paid, $66.00, average satisfaction 79.2 |
| `runs/stage-1/defeat.txt` | DEFEAT: 3 paid, $42.00, average satisfaction 65.3 |
| `runs/stage-2/victory.txt` | VICTORY: 5 paid, $73.50, average satisfaction 75.6 |
| `runs/stage-2/defeat.txt` | DEFEAT: 4 paid, $58.50, average satisfaction 54.3 |
| `runs/stage-2/critic-leaves.txt` | The ignored Critic leaves at 0 and her order is cancelled (VICTORY: 4 paid, $55.50, 83.5) |

## Design documents

[docs/uml/class-diagram.png](docs/uml/class-diagram.png) (and the
[SVG](docs/uml/class-diagram.svg), which stays sharp when zoomed) is the class diagram of the Stage 2
code, in the UML notation from the course's class diagram lecture.

## Stages and changes

| Stage | Adds | Tag |
| --- | --- | --- |
| 1 Basic operation | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings | [`stage-1`](https://github.com/WengJian02/SC2002-Restaurant-Rush/tree/stage-1) |
| 2 Customer behaviour | VIP and Critic customers | [`stage-2`](https://github.com/WengJian02/SC2002-Restaurant-Rush/tree/stage-2) (this version) |
| 3 New role and menu policy | Host, ComboMeal, Happy Hour | `stage-3` |

Every tag is a runnable commit: check it out and use `./run.sh` and `./test.sh` as above.

### Stage 1 to Stage 2

Compare: https://github.com/WengJian02/SC2002-Restaurant-Rush/compare/stage-1...stage-2

| File | Change | Reason |
| --- | --- | --- |
| `entity/VIPCustomer.java` | New | Overrides `getSatisfactionLoss()` (5) and `calculateBill()` (10% off). |
| `entity/CriticCustomer.java` | New | Overrides `getSatisfactionLoss()` (12) and `chooseMenuItem()` (most demanding dish). |
| `control/GameSetup.java` | Changed (setup only) | The arrivals now create a VIP (turn 4) and a Critic (turn 10) in place of two Regulars. |
| `util/Money.java` | Changed (one method added) | `percentOf()` rounds a percentage to the nearest cent, which the VIP discount needs. |
| `test/restaurant/RulesTest.java` | Changed | New checks for VIP and Critic behaviour, for the same flow for every type and for no type checks in the controller. Because the arrivals changed, the Stage 1 replays became Stage 2 replays and the "all customers are Regular" check now checks the new arrivals (the Stage 1 runs are replayed at `stage-1`). The other Stage 1 checks are unchanged. |
| `runs/stage-2/*` | New | Scripted Stage 2 victory, defeat and Critic-leaves runs. |
| `docs/uml/class-diagram.png`, `.svg` | Changed | The class diagram now shows `VIPCustomer`, `CriticCustomer` and `Money.percentOf()`, with a note on polymorphism. |
| `README.md`, `docs/TEST_NOTES.md` | Changed | Stage 2 rules, runs and this change log. |
| Deleted files | None | |

`GameController`, `RestaurantCLI`, `Customer`, the staff classes and `Order` did not change. Stage 1
already asked each customer object for its satisfaction loss, its bill and its food choice, so the two
new types only had to override those methods.
