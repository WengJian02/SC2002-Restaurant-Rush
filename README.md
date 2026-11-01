# Restaurant Rush

Restaurant Rush is a turn-based restaurant game for the command line, written in Java for our SC2002
group assignment. You play the manager. Each staff member can do one task per turn, so every turn you
decide which customers and orders the Host, the Waiter and the Chef deal with first.

This is Stage 3, the final version (git tag `stage-3`). It adds a Host, combo meals and Happy Hour to
the game from the earlier tags `stage-1` and `stage-2`. What changed between the stages is listed under
[Stages and changes](#stages-and-changes).

Repository: https://github.com/WengJian02/SC2002-Restaurant-Rush

Lab group BACF1, group 5.

| Section | |
| --- | --- |
| [Build and run](#build-and-run) | [Rules of the game](#rules-of-the-game) |
| [Base setting](#base-setting) | [Project structure](#project-structure) |
| [Customers](#customers) | [Tests and reproducible runs](#tests-and-reproducible-runs) |
| [How a turn works](#how-a-turn-works) | [Design documents](#design-documents) |
| [Stages and changes](#stages-and-changes) | [Video timestamps](#video-timestamps) |
| [AI chat history](#ai-chat-history) | |

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
and `0` always means wait, no or go back.

## Base setting

| Setting | Value |
| --- | --- |
| Turns | 18 (the game always plays all 18) |
| Tables | 2 |
| Staff | Host x1 (seats one queued customer per turn), Waiter x1 (one task per turn), Chef x1 (one preparation unit per turn), Cashier x1 (one payment per turn, automatic) |
| Arrivals | One customer at the start of turns 1, 4, 7, 10, 13 and 16 |
| Objective | After turn 18: at least 4 paid customers, revenue of at least $45.00 and an average satisfaction of paid customers of at least 60. All three are needed to win. |

| Menu item | Price | Preparation units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger Combo | $21.60 | 2 |
| Salad + Pasta Combo | $24.30 | 3 |
| Burger + Pasta Combo | $29.70 | 3 |

A combo is made of two of the dishes above. Its preparation units are the two dishes' units added
together, and its price is 90% of their combined price, rounded to the nearest cent.

## Customers

| Type | Satisfaction lost per waiting turn | Bill | Special behaviour |
| --- | --- | --- | --- |
| Regular | 8 | Full price | none |
| VIP | 5 | 10% off | none |
| Critic | 12 | Full price | Always orders the most demanding item (see below) |

Arrivals in the base setting: #1 Alice (Regular, turn 1), #2 Victor (VIP, turn 4), #3 Chloe (Regular,
turn 7), #4 Carmen (Critic, turn 10), #5 Ethan (Regular, turn 13) and #6 Fiona (Regular, turn 16). The
VIP and the Critic take the place of two Stage 1 Regulars, so there are still six arrivals. Every type
goes through the same visit (seated, orders, eats, pays). The customer classes decide what differs,
and the turn controller never checks a customer's type.

## How a turn works

1. Arrivals. The customer due this turn joins the waiting queue with satisfaction 100. If Happy Hour
   is in its second turn, customers waiting for a table or for food recover 15 satisfaction.
2. Manager choices. The game shows the turn, revenue, the average satisfaction of paid customers, the
   Happy Hour status, the waiting queue, the tables with each customer's satisfaction and order status,
   and what each staff member could do. Then it asks, in this order:
   * whether to switch Happy Hour on (only while it has not been used);
   * which queued customer the Host seats, or whether the Host waits;
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
  the n-th item on the menu, starting again from the top when the menu runs out. The choice and the
  bill are printed as soon as the order is placed.
* The Stage 3 menu lists the three combos after the three dishes, and the same food rule picks from
  it. A customer therefore orders a combo when their arrival number lands on one: customer 5 (Ethan)
  orders the Salad + Pasta Combo and customer 6 (Fiona) the Burger + Pasta Combo. Customers 1 to 3
  still order Salad, Burger and Pasta.
* A Critic ignores the food rule and orders the most demanding item on the menu, to test the kitchen:
  the item with the most preparation units, and the more expensive one if two are tied. On the
  Stage 2 menu that is Pasta (customer #4 would otherwise get Salad). On the Stage 3 menu it is the
  3-unit Burger + Pasta Combo ($29.70). Since a Critic also loses 12 per waiting turn, slow service
  costs the most with a Critic.
* The VIP discount is 10% off the menu price, rounded to the nearest cent ($15.00 becomes $13.50).
* Happy Hour can be switched on once per game, at the start of a turn before any staff member acts.
  It lasts that turn and the next, and the status line shows the turns left. Orders taken while it is
  on get 20% off, applied after the VIP discount, and the price is locked into the order (a VIP Burger
  costs $15.00 x 0.9 x 0.8 = $10.80). Customers waiting for a table or for food recover 15 satisfaction
  (never above 100) when it is switched on and again at the start of its second turn. Orders already
  placed and customers who have left are not affected.
* The Host seats at most one queued customer per turn, on top of the Waiter's task, and is asked
  before the Waiter, so a customer the Host seats can give their order to the Waiter in the same turn.
  The Waiter can still seat customers too.
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
  entity/MenuItem, ComboMeal, Menu, Order               food and orders
  entity/Staff, Host, Waiter, Chef, Cashier, Seater     staff (one task per turn each)
  entity/Table, WaitingQueue                            where customers wait
  entity/HappyHour                                      Happy Hour pricing policy
  entity/Scoreboard, Objective                          results and the three targets
  entity/CustomerStatus, OrderStatus                    the two status enums
  util/Money                 formats cents as dollars and works out percentages
test/restaurant/RulesTest    rule checks and replays of the scripted runs
runs/stage-N/                scripted runs for each stage, with their transcripts
docs/TEST_NOTES.md           test notes for every scripted run
docs/uml/                    class diagrams and the sequence diagram
```

## Tests and reproducible runs

The game has many small rules that depend on each other across turns. Checking them by playing a
full 18-turn game after every change was slow, and it was easy to miss a rule that had stopped
working, so we wrote the rules down as checks that run in a few seconds. When we added Stage 2 and
Stage 3, the earlier checks showed straight away whether the rules we already had still held.

`./test.sh` (Windows: `test.bat`) runs 131 rule checks, including the brief's Section 3.3 example
turn by turn and what happens when the input is unexpected: a word instead of a number, an option
that does not exist, a customer who is not there, or no input left. It then replays this stage's runs
through the real command-line interface and compares the output with the saved transcripts.

[docs/TEST_NOTES.md](docs/TEST_NOTES.md) goes through one victory run and one defeat run for every
stage, turn by turn. To watch a run, feed its input file to the game. `--echo` prints each answer next
to its prompt, so the output reads like a live game:

```
./run.sh --echo < runs/stage-3/victory.txt
./run.sh --echo < runs/stage-3/defeat.txt
```

On Windows: `run.bat --echo < runs\stage-3\victory.txt`. Each stage's runs belong to that stage's
arrivals and menu, so replay `runs/stage-1` after `git checkout stage-1` and `runs/stage-2` after
`git checkout stage-2`. The full output of every run is saved next to it as `*-transcript.txt`.

| Run | Result |
| --- | --- |
| `runs/stage-1/victory.txt` | VICTORY: 5 paid, $66.00, average satisfaction 79.2 |
| `runs/stage-1/defeat.txt` | DEFEAT: 3 paid, $42.00, average satisfaction 65.3 (one table used at a time) |
| `runs/stage-2/victory.txt` | VICTORY: 5 paid, $73.50, average satisfaction 75.6 |
| `runs/stage-2/defeat.txt` | DEFEAT: 4 paid, $58.50, average satisfaction 54.3 (slow, badly prioritised service) |
| `runs/stage-2/critic-leaves.txt` | The ignored Critic leaves at 0 and her order is cancelled (VICTORY: 4 paid, $55.50, 83.5) |
| `runs/stage-3/victory.txt` | VICTORY: 5 paid, $91.80, average satisfaction 80.8 (Happy Hour on the VIP's order) |
| `runs/stage-3/happy-hour-late.txt` | Same game with Happy Hour on turn 13 instead (VICTORY: 5 paid, $89.64, 87.4) |
| `runs/stage-3/defeat.txt` | DEFEAT: 3 paid, $38.70, average satisfaction 65.7; the neglected Critic leaves |

## Design documents

All diagrams are in `docs/uml`. They use the UML notation from the course's class diagram and
sequence diagram lectures. The SVG files stay sharp when zoomed.

| Document | Files |
| --- | --- |
| Class diagram of the final (Stage 3) code, with every attribute and operation, all relationships and the notes on encapsulation, inheritance, polymorphism and composition | [PNG](docs/uml/class-diagram.png), [SVG](docs/uml/class-diagram.svg) |
| The same class diagram split into six smaller views | 1 boundary and setup: [PNG](docs/uml/class-diagram-view-1-boundary.png), [SVG](docs/uml/class-diagram-view-1-boundary.svg); 2 the game controller: [PNG](docs/uml/class-diagram-view-2-controller.png), [SVG](docs/uml/class-diagram-view-2-controller.svg); 3 customers: [PNG](docs/uml/class-diagram-view-3-customers.png), [SVG](docs/uml/class-diagram-view-3-customers.svg); 4 orders and the menu: [PNG](docs/uml/class-diagram-view-4-orders-menu.png), [SVG](docs/uml/class-diagram-view-4-orders-menu.svg); 5 staff: [PNG](docs/uml/class-diagram-view-5-staff.png), [SVG](docs/uml/class-diagram-view-5-staff.svg); 6 Happy Hour and results: [PNG](docs/uml/class-diagram-view-6-happy-hour-results.png), [SVG](docs/uml/class-diagram-view-6-happy-hour-results.svg) |
| Sequence diagram VIPVisitDuringHappyHour: Victor's visit in the Stage 3 victory run, from his arrival on turn 4 to his payment on turn 7, then the result check (five pages) | PNG pages [1](docs/uml/sequence-diagram-1.png), [2](docs/uml/sequence-diagram-2.png), [3](docs/uml/sequence-diagram-3.png), [4](docs/uml/sequence-diagram-4.png), [5](docs/uml/sequence-diagram-5.png); SVG pages [1](docs/uml/sequence-diagram-1.svg), [2](docs/uml/sequence-diagram-2.svg), [3](docs/uml/sequence-diagram-3.svg), [4](docs/uml/sequence-diagram-4.svg), [5](docs/uml/sequence-diagram-5.svg); [PlantUML source](docs/uml/sequence-diagram.puml) |
| Sequence diagram EndTurn: the end-of-turn steps, which the diagram above refers to in a `ref` fragment | [PNG](docs/uml/sequence-diagram-endturn.png), [SVG](docs/uml/sequence-diagram-endturn.svg), [PlantUML source](docs/uml/sequence-diagram-endturn.puml) |
| Report (PDF) | Submitted in the zip together with this project |

Each stage tag has its own `docs/uml/class-diagram` for that stage's code, so there is one class
diagram per stage, and each matches the code at its tag:

| Stage | Class diagram |
| --- | --- |
| Stage 1 | [PNG](https://github.com/WengJian02/SC2002-Restaurant-Rush/blob/stage-1/docs/uml/class-diagram.png), [SVG](https://github.com/WengJian02/SC2002-Restaurant-Rush/blob/stage-1/docs/uml/class-diagram.svg) (at the `stage-1` tag) |
| Stage 2 | [PNG](https://github.com/WengJian02/SC2002-Restaurant-Rush/blob/stage-2/docs/uml/class-diagram.png), [SVG](https://github.com/WengJian02/SC2002-Restaurant-Rush/blob/stage-2/docs/uml/class-diagram.svg) (at the `stage-2` tag) |
| Stage 3 | [PNG](docs/uml/class-diagram.png), [SVG](docs/uml/class-diagram.svg) (this version) |

The sequence diagrams were rendered with PlantUML 1.2025.4:
`java -jar plantuml.jar -tsvg docs/uml/sequence-diagram.puml docs/uml/sequence-diagram-endturn.puml`,
and with `-tpng -Sdpi=144` for the PNG files. PlantUML writes the five pages as `sequence-diagram`
and `sequence-diagram_001` to `_004`; here they are saved as `sequence-diagram-1` to `-5`.

## Stages and changes

| Stage | Adds | Tag |
| --- | --- | --- |
| 1 Basic operation | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings | [`stage-1`](https://github.com/WengJian02/SC2002-Restaurant-Rush/tree/stage-1) |
| 2 Customer behaviour | VIP and Critic customers | [`stage-2`](https://github.com/WengJian02/SC2002-Restaurant-Rush/tree/stage-2) |
| 3 New role and menu policy | Host, ComboMeal, Happy Hour | [`stage-3`](https://github.com/WengJian02/SC2002-Restaurant-Rush/tree/stage-3) |

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

### Stage 2 to Stage 3

Compare: https://github.com/WengJian02/SC2002-Restaurant-Rush/compare/stage-2...stage-3

| File | Change | Reason |
| --- | --- | --- |
| `entity/ComboMeal.java` | New | A `MenuItem` made of two `MenuItem`s: units added together, price 90% rounded to the cent. Since it is a `MenuItem`, `Order`, `Chef`, `Customer` and `CriticCustomer` handle it unchanged. |
| `entity/Host.java` | New | A `Staff` member that only seats customers, one per turn. |
| `entity/Seater.java` | New | A small interface for staff who can seat a queued customer. The Waiter and the Host both implement it, so the controller's seating rule is written once. |
| `entity/HappyHour.java` | New | The pricing policy: once per game, lasts two turns, 20% off orders taken, 15 satisfaction recovery. |
| `entity/Waiter.java` | Changed (declaration only) | Now `implements Seater`, with `@Override` on `seat()`. Its behaviour is the same. |
| `entity/Customer.java` | Changed (one method added) | `recoverSatisfaction()`. Happy Hour is the first rule that raises satisfaction, and the 0 to 100 limits stay inside `Customer`. |
| `control/GameController.java` | Changed | Adds the Host (`hostSeat`, `hostWait`, `hostHasTask`) and moves the Waiter's seating checks into one `seatFromQueue(Seater, ...)` that the Waiter and the Host share. Adds Happy Hour: switching it on at the start of a turn, the recovery, the discount after the customer's own discount when an order is taken, and the countdown in `endTurn()`. The order message now shows the menu price when a discount applies. The six turn steps keep their order and their earlier rules. |
| `control/GameSetup.java` | Changed (setup only) | Puts the three combos on the menu. |
| `boundary/RestaurantCLI.java` | Changed | Shows the Happy Hour status and turns left, asks about Happy Hour and the Host, and lists the Host's task. The welcome screen's staff list needed no change because it is built from `Staff`. |
| `test/restaurant/RulesTest.java` | Changed | New checks for combos, the Host and Happy Hour, and the Stage 3 replays. Five earlier checks were updated because the setting grew: the staff check now expects the Host, the base-menu check no longer expects exactly three items, the food-rule check and the Critic's choice check build the three-dish menu themselves, and the input-handling check answers the new Happy Hour and Host questions first. The test helper `gameWith()` also builds the three-dish menu, so the Stage 1 and 2 checks still get the same orders. The Stage 2 runs are replayed at `stage-2`. |
| `runs/stage-3/*` | New | Scripted Stage 3 victory, defeat and Happy Hour timing runs. |
| `docs/uml/class-diagram.png`, `.svg` | Changed | The final class diagram, with `Host`, `Seater`, `ComboMeal`, `HappyHour` and the new members. |
| `docs/uml/class-diagram-view-*` | New | The final class diagram in six smaller views. |
| `docs/uml/sequence-diagram*` | New | The sequence diagram (VIP visit during Happy Hour) and the `EndTurn` diagram it refers to, with their PlantUML sources. |
| `README.md`, `docs/TEST_NOTES.md` | Changed | Stage 3 rules, runs and this change log. |
| Deleted files | None | |

No game rule from Stage 1 or 2 was rewritten. Those stages' rule checks (including the brief's
Section 3.3 example, abandonment, the VIP and the Critic) still pass on the Stage 3 code. Only the five
checks listed above, which describe the setting itself, were updated.

## Video timestamps

Video: _add the link or file name here_

The video follows the briefing's suggested 15-minute outline, and each member presents a part.

| Part | Content | Starts at | Presented by |
| --- | --- | --- | --- |
| 1 | Game overview and the three stages | mm:ss | |
| 2 | Gameplay demonstration and key outcomes | mm:ss | |
| 3 | Class diagrams and evidence of stage changes | mm:ss | |
| 4 | Use case and sequence diagram | mm:ss | |
| 5 | Individual contributions and key learning | mm:ss | |

Where the brief's seven segments appear in the video:

| # | Segment | Part | Timestamp |
| --- | --- | --- | --- |
| 1 | The game launching from the submitted project | 1 | mm:ss |
| 2 | Meaningful manager choices, including a decision where staff are constrained | 2 | mm:ss |
| 3 | A dish going READY -> SERVED -> payment | 2 | mm:ss |
| 4 | A VIP or Critic behaviour | 2 | mm:ss |
| 5 | A Stage 3 feature | 2 | mm:ss |
| 6 | One victory and one defeat | 2 | mm:ss |
| 7 | The stage tag comparisons and the diagrams | 3 | mm:ss |

## AI chat history

We used AI (Claude) in this project. Only Kwok Weng Jian has an AI chat history. It is in the
[`ai-history`](https://github.com/WengJian02/SC2002-Restaurant-Rush/tree/ai-kwok-weng-jian/ai-history)
folder on the `ai-kwok-weng-jian` branch of this repository. As our course instructed, it holds the
exchanges in which the AI was asked to write or change the code, with the messages and replies
copied word for word and in order.
