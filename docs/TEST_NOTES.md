# Test notes

The game has no randomness, so the same answers always give the same game and every run below
can be reproduced. Each run is a plain text file of answers, one per line, and any text after `//`
is a note. Replay a run with `--echo` to see each answer printed next to its prompt:

```
./run.sh --echo < runs/stage-2/victory.txt        (Windows: run.bat --echo < runs\stage-2\victory.txt)
```

The complete output of each run is saved next to it as `*-transcript.txt`. `./test.sh` replays
every run of the current stage and checks that the output still matches its transcript exactly.

In the tables, "Waiting" lists every customer who lost satisfaction at the end of the turn
because they were still waiting for a table or for food.

## Stage 1 (tag `stage-1`)

Replay these runs after `git checkout stage-1`: they use the Stage 1 arrivals (all Regular).

Arrivals (all Regular, -8 per waiting turn): #1 Alice (turn 1), #2 Ben (turn 4), #3 Chloe (turn 7),
#4 Daniel (turn 10), #5 Ethan (turn 13), #6 Fiona (turn 16). Under the food rule they order
Salad, Burger, Pasta, Salad, Burger and Pasta.

### Victory run: `runs/stage-1/victory.txt`

The manager uses both tables and keeps every customer moving: each customer is seated on arrival,
the order is taken the next turn and the Chef cooks it straight away. On turns 10, 13 and 16 the
Waiter is constrained, because a dish is READY just as a new customer arrives. The manager serves
first, so the served customer stops losing satisfaction and their table is freed sooner.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Waiter seats Alice (Table 1)<br>Chef: nothing to do | Waiting: Alice 92 |
| 2 |  | Waiter takes Alice's order: Salad, bill $9.00<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 84 |
| 3 |  | Waiter serves Alice's Salad<br>Chef: nothing to do |  |
| 4 | #2 Ben (Regular) | Waiter seats Ben (Table 2)<br>Chef: nothing to do | Alice READY_TO_PAY<br>Waiting: Ben 92 |
| 5 |  | Waiter takes Ben's order: Burger, bill $15.00<br>Chef: Ben's Burger 1/1, READY | Alice pays $9.00 (satisfaction 84)<br>Waiting: Ben 84 |
| 6 |  | Waiter serves Ben's Burger<br>Chef: nothing to do |  |
| 7 | #3 Chloe (Regular) | Waiter seats Chloe (Table 1)<br>Chef: nothing to do | Ben READY_TO_PAY<br>Waiting: Chloe 92 |
| 8 |  | Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef: Chloe's Pasta 1/2 | Ben pays $15.00 (satisfaction 84)<br>Waiting: Chloe 84 |
| 9 |  | Waiter: nothing to do<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 76 |
| 10 | #4 Daniel (Regular) | Waiter serves Chloe's Pasta<br>Chef: nothing to do | Waiting: Daniel 92 |
| 11 |  | Waiter seats Daniel (Table 2)<br>Chef: nothing to do | Chloe READY_TO_PAY<br>Waiting: Daniel 84 |
| 12 |  | Waiter takes Daniel's order: Salad, bill $9.00<br>Chef: Daniel's Salad 1/1, READY | Chloe pays $18.00 (satisfaction 76)<br>Waiting: Daniel 76 |
| 13 | #5 Ethan (Regular) | Waiter serves Daniel's Salad<br>Chef: nothing to do | Waiting: Ethan 92 |
| 14 |  | Waiter seats Ethan (Table 1)<br>Chef: nothing to do | Daniel READY_TO_PAY<br>Waiting: Ethan 84 |
| 15 |  | Waiter takes Ethan's order: Burger, bill $15.00<br>Chef: Ethan's Burger 1/1, READY | Daniel pays $9.00 (satisfaction 76)<br>Waiting: Ethan 76 |
| 16 | #6 Fiona (Regular) | Waiter serves Ethan's Burger<br>Chef: nothing to do | Waiting: Fiona 92 |
| 17 |  | Waiter seats Fiona (Table 2)<br>Chef: nothing to do | Ethan READY_TO_PAY<br>Waiting: Fiona 84 |
| 18 |  | Waiter takes Fiona's order: Pasta, bill $18.00<br>Chef: Fiona's Pasta 1/2 | Ethan pays $15.00 (satisfaction 76)<br>Waiting: Fiona 76 |

Result: VICTORY. Five customers paid (Alice, Ben, Chloe, Daniel, Ethan), revenue is $66.00
(9 + 15 + 18 + 9 + 15) and the average satisfaction is 79.2 = (84 + 84 + 76 + 76 + 76) / 5.
Fiona arrives on turn 16 and cannot finish paying within 18 turns, as the brief intends.

### Defeat run: `runs/stage-1/defeat.txt`

The mistake: the manager only ever uses one table. The next customer is seated only after the
previous one has paid, even though the second table is free. The run also has a word typed instead
of a number (turn 1) and two refused Waiter choices (turns 8 and 13), and none of them uses up the
Waiter's task.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Input rejected: "seat" is not a number. Please enter a number.<br>Waiter seats Alice (Table 1)<br>Chef: nothing to do | Waiting: Alice 92 |
| 2 |  | Waiter takes Alice's order: Salad, bill $9.00<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 84 |
| 3 |  | Waiter serves Alice's Salad<br>Chef: nothing to do |  |
| 4 | #2 Ben (Regular) | Waiter waits<br>Chef: nothing to do | Alice READY_TO_PAY<br>Waiting: Ben 92 |
| 5 |  | Waiter waits<br>Chef: nothing to do | Alice pays $9.00 (satisfaction 84)<br>Waiting: Ben 84 |
| 6 |  | Waiter seats Ben (Table 1)<br>Chef: nothing to do | Waiting: Ben 76 |
| 7 | #3 Chloe (Regular) | Waiter takes Ben's order: Burger, bill $15.00<br>Chef: Ben's Burger 1/1, READY | Waiting: Ben 68, Chloe 92 |
| 8 |  | Refused: Chloe has not ordered yet, so there is nothing to serve.<br>Waiter serves Ben's Burger<br>Chef: nothing to do | Waiting: Chloe 84 |
| 9 |  | Waiter waits<br>Chef: nothing to do | Ben READY_TO_PAY<br>Waiting: Chloe 76 |
| 10 | #4 Daniel (Regular) | Waiter waits<br>Chef: nothing to do | Ben pays $15.00 (satisfaction 68)<br>Waiting: Chloe 68, Daniel 92 |
| 11 |  | Waiter seats Chloe (Table 1)<br>Chef: nothing to do | Waiting: Chloe 60, Daniel 84 |
| 12 |  | Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef: Chloe's Pasta 1/2 | Waiting: Chloe 52, Daniel 76 |
| 13 | #5 Ethan (Regular) | Refused: Chloe's Pasta is not READY yet (1 of 2 preparation units done).<br>Waiter waits<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 44, Daniel 68, Ethan 92 |
| 14 |  | Waiter serves Chloe's Pasta<br>Chef: nothing to do | Waiting: Daniel 60, Ethan 84 |
| 15 |  | Waiter waits<br>Chef: nothing to do | Chloe READY_TO_PAY<br>Waiting: Daniel 52, Ethan 76 |
| 16 | #6 Fiona (Regular) | Waiter waits<br>Chef: nothing to do | Chloe pays $18.00 (satisfaction 44)<br>Waiting: Daniel 44, Ethan 68, Fiona 92 |
| 17 |  | Waiter seats Daniel (Table 1)<br>Chef: nothing to do | Waiting: Daniel 36, Ethan 60, Fiona 84 |
| 18 |  | Waiter takes Daniel's order: Salad, bill $9.00<br>Chef: Daniel's Salad 1/1, READY | Waiting: Daniel 28, Ethan 52, Fiona 76 |

Result: DEFEAT. Only 3 customers paid (target 4) and revenue is $42.00 (target $45.00). The average
satisfaction of the three who paid is 65.3 ((84 + 68 + 44) / 3 = 65.33), which meets its target, but
the other two targets are missed. The customers are the same as in the victory run; only the
manager's seating decisions differ.

### Rule checks in `test/restaurant/RulesTest.java`

At tag `stage-1`, `./test.sh` runs 78 checks:

| Group | What is checked |
| --- | --- |
| Base setting | 18 turns, 2 tables, Waiter/Chef/Cashier, arrival turns, menu prices and units, the three targets, satisfaction 100 on arrival |
| Section 3.3 example | The brief's single-customer Pasta example, turn by turn: 92, 84, 76, READY, SERVED, READY_TO_PAY, PAID $18.00 with 76 recorded, table freed |
| One task per turn | A second Waiter task is refused; the `Staff` class itself never allows two tasks |
| Refused choices | Ordering from the queue, serving without an order, seating someone not queued: refused with a reason, task not used |
| Waiter before Chef | The Chef cannot act first; the Chef may cook an order taken this turn; one unit per turn; a dish READY before the Waiter acts can be served |
| Tables | Seating is refused while both tables are occupied; a `Table` refuses a second customer |
| Eating and payment | No eating before being served; only a READY order can be served; the Cashier charges one customer per turn, oldest first, never before READY_TO_PAY, and not on the turn they became READY_TO_PAY |
| Abandonment | -8 per waiting turn, never below 0; at 0 the customer leaves, the order is CANCELLED, the table freed, the departure counted, revenue unchanged |
| Food rule | Customers 1-6 order Salad, Burger, Pasta, Salad, Burger, Pasta |
| Last arrival | Even with perfect play the turn-16 customer cannot pay by turn 18 |
| Result | Victory needs all three targets (boundary values 4 paid, $45.00, average 60 are met; 59.75 is not) |
| Money and input | Cents shown as dollars; text, wrong options and invalid customers are rejected and asked again; running out of input still finishes the game |
| Replays | Both runs above give the expected result and match their saved transcripts |

## Stage 2 (tag `stage-2`)

Arrivals: #1 Alice (Regular, turn 1), #2 Victor (VIP, turn 4), #3 Chloe (Regular, turn 7),
#4 Carmen (Critic, turn 10), #5 Ethan (Regular, turn 13), #6 Fiona (Regular, turn 16).
They order Salad, Burger ($13.50 for the VIP), Pasta, Pasta (the Critic's most demanding dish),
Burger and Pasta. Satisfaction lost per waiting turn: Regular 8, VIP 5, Critic 12.

### Victory run: `runs/stage-2/victory.txt`

The manager follows the Stage 1 plan: seat on arrival, take the order the next turn, cook straight
away and serve READY dishes first. On turn 14 the Waiter must choose between serving the Critic and
taking Ethan's order. The Critic loses 12 per turn, so she is served first.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Waiter seats Alice (Table 1)<br>Chef: nothing to do | Waiting: Alice 92 |
| 2 |  | Waiter takes Alice's order: Salad, bill $9.00<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 84 |
| 3 |  | Waiter serves Alice's Salad<br>Chef: nothing to do |  |
| 4 | #2 Victor (VIP) | Waiter seats Victor (Table 2)<br>Chef: nothing to do | Alice READY_TO_PAY<br>Waiting: Victor 95 |
| 5 |  | Waiter takes Victor's order: Burger, bill $13.50<br>Chef: Victor's Burger 1/1, READY | Alice pays $9.00 (satisfaction 84)<br>Waiting: Victor 90 |
| 6 |  | Waiter serves Victor's Burger<br>Chef: nothing to do |  |
| 7 | #3 Chloe (Regular) | Waiter seats Chloe (Table 1)<br>Chef: nothing to do | Victor READY_TO_PAY<br>Waiting: Chloe 92 |
| 8 |  | Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef: Chloe's Pasta 1/2 | Victor pays $13.50 (satisfaction 90)<br>Waiting: Chloe 84 |
| 9 |  | Waiter: nothing to do<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 76 |
| 10 | #4 Carmen (Critic) | Waiter serves Chloe's Pasta<br>Chef: nothing to do | Waiting: Carmen 88 |
| 11 |  | Waiter seats Carmen (Table 2)<br>Chef: nothing to do | Chloe READY_TO_PAY<br>Waiting: Carmen 76 |
| 12 |  | Waiter takes Carmen's order: Pasta, bill $18.00<br>Chef: Carmen's Pasta 1/2 | Chloe pays $18.00 (satisfaction 76)<br>Waiting: Carmen 64 |
| 13 | #5 Ethan (Regular) | Waiter seats Ethan (Table 1)<br>Chef: Carmen's Pasta 2/2, READY | Waiting: Carmen 52, Ethan 92 |
| 14 |  | Waiter serves Carmen's Pasta<br>Chef: nothing to do | Waiting: Ethan 84 |
| 15 |  | Waiter takes Ethan's order: Burger, bill $15.00<br>Chef: Ethan's Burger 1/1, READY | Carmen READY_TO_PAY<br>Waiting: Ethan 76 |
| 16 | #6 Fiona (Regular) | Waiter serves Ethan's Burger<br>Chef: nothing to do | Carmen pays $18.00 (satisfaction 52)<br>Waiting: Fiona 92 |
| 17 |  | Waiter seats Fiona (Table 2)<br>Chef: nothing to do | Ethan READY_TO_PAY<br>Waiting: Fiona 84 |
| 18 |  | Waiter takes Fiona's order: Pasta, bill $18.00<br>Chef: Fiona's Pasta 1/2 | Ethan pays $15.00 (satisfaction 76)<br>Waiting: Fiona 76 |

Result: VICTORY. 5 paid, revenue $73.50 (9 + 13.50 + 18 + 18 + 15), average satisfaction
75.6 = (84 + 90 + 76 + 52 + 76) / 5. Victor, the VIP, still has 90 when he pays because he loses only
5 per turn.

### Defeat run: `runs/stage-2/defeat.txt`

The mistakes: the Waiter always seats new customers and takes orders before serving READY dishes,
and the Chef is kept waiting on the turn each order is taken, as if orders had to be batched. Every
dish reaches its customer late.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Waiter seats Alice (Table 1)<br>Chef: nothing to do | Waiting: Alice 92 |
| 2 |  | Waiter takes Alice's order: Salad, bill $9.00<br>Chef waits | Waiting: Alice 84 |
| 3 |  | Waiter: nothing to do<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 76 |
| 4 | #2 Victor (VIP) | Waiter seats Victor (Table 2)<br>Chef: nothing to do | Waiting: Alice 68, Victor 95 |
| 5 |  | Waiter takes Victor's order: Burger, bill $13.50<br>Chef waits | Waiting: Alice 60, Victor 90 |
| 6 |  | Waiter serves Alice's Salad<br>Chef: Victor's Burger 1/1, READY | Waiting: Victor 85 |
| 7 | #3 Chloe (Regular) | Waiter serves Victor's Burger<br>Chef: nothing to do | Alice READY_TO_PAY<br>Waiting: Chloe 92 |
| 8 |  | Waiter: nothing to do<br>Chef: nothing to do | Victor READY_TO_PAY<br>Alice pays $9.00 (satisfaction 60)<br>Waiting: Chloe 84 |
| 9 |  | Waiter seats Chloe (Table 1)<br>Chef: nothing to do | Victor pays $13.50 (satisfaction 85)<br>Waiting: Chloe 76 |
| 10 | #4 Carmen (Critic) | Waiter seats Carmen (Table 2)<br>Chef: nothing to do | Waiting: Chloe 68, Carmen 88 |
| 11 |  | Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef waits | Waiting: Chloe 60, Carmen 76 |
| 12 |  | Waiter takes Carmen's order: Pasta, bill $18.00<br>Chef: Chloe's Pasta 1/2 | Waiting: Chloe 52, Carmen 64 |
| 13 | #5 Ethan (Regular) | Waiter: nothing to do<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 44, Carmen 52, Ethan 92 |
| 14 |  | Waiter serves Chloe's Pasta<br>Chef: Carmen's Pasta 1/2 | Waiting: Carmen 40, Ethan 84 |
| 15 |  | Waiter: nothing to do<br>Chef: Carmen's Pasta 2/2, READY | Chloe READY_TO_PAY<br>Waiting: Carmen 28, Ethan 76 |
| 16 | #6 Fiona (Regular) | Waiter serves Carmen's Pasta<br>Chef: nothing to do | Chloe pays $18.00 (satisfaction 44)<br>Waiting: Ethan 68, Fiona 92 |
| 17 |  | Waiter seats Ethan (Table 1)<br>Chef: nothing to do | Carmen READY_TO_PAY<br>Waiting: Ethan 60, Fiona 84 |
| 18 |  | Waiter takes Ethan's order: Burger, bill $15.00<br>Chef waits | Carmen pays $18.00 (satisfaction 28)<br>Waiting: Ethan 52, Fiona 76 |

Result: DEFEAT. 4 paid and revenue $58.50 meet their targets, but the average satisfaction is
54.3 ((60 + 85 + 44 + 28) / 4 = 54.25), below 60. The Critic loses 12 per turn and ordered the slow
Pasta, so she pays with only 28 and pulls the average down. The victory run served the same
customers with different priorities.

### Extra run, the ignored Critic leaves: `runs/stage-2/critic-leaves.txt`

Turns 1-11 match the victory run. From turn 12 the manager never lets the Chef cook Carmen's Pasta.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Waiter seats Alice (Table 1)<br>Chef: nothing to do | Waiting: Alice 92 |
| 2 |  | Waiter takes Alice's order: Salad, bill $9.00<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 84 |
| 3 |  | Waiter serves Alice's Salad<br>Chef: nothing to do |  |
| 4 | #2 Victor (VIP) | Waiter seats Victor (Table 2)<br>Chef: nothing to do | Alice READY_TO_PAY<br>Waiting: Victor 95 |
| 5 |  | Waiter takes Victor's order: Burger, bill $13.50<br>Chef: Victor's Burger 1/1, READY | Alice pays $9.00 (satisfaction 84)<br>Waiting: Victor 90 |
| 6 |  | Waiter serves Victor's Burger<br>Chef: nothing to do |  |
| 7 | #3 Chloe (Regular) | Waiter seats Chloe (Table 1)<br>Chef: nothing to do | Victor READY_TO_PAY<br>Waiting: Chloe 92 |
| 8 |  | Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef: Chloe's Pasta 1/2 | Victor pays $13.50 (satisfaction 90)<br>Waiting: Chloe 84 |
| 9 |  | Waiter: nothing to do<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 76 |
| 10 | #4 Carmen (Critic) | Waiter serves Chloe's Pasta<br>Chef: nothing to do | Waiting: Carmen 88 |
| 11 |  | Waiter seats Carmen (Table 2)<br>Chef: nothing to do | Chloe READY_TO_PAY<br>Waiting: Carmen 76 |
| 12 |  | Waiter takes Carmen's order: Pasta, bill $18.00<br>Chef waits | Chloe pays $18.00 (satisfaction 76)<br>Waiting: Carmen 64 |
| 13 | #5 Ethan (Regular) | Waiter seats Ethan (Table 1)<br>Chef waits | Waiting: Carmen 52, Ethan 92 |
| 14 |  | Waiter takes Ethan's order: Burger, bill $15.00<br>Chef: Ethan's Burger 1/1, READY | Waiting: Carmen 40, Ethan 84 |
| 15 |  | Waiter serves Ethan's Burger<br>Chef waits | Waiting: Carmen 28 |
| 16 | #6 Fiona (Regular) | Waiter: nothing to do<br>Chef waits | Ethan READY_TO_PAY<br>Waiting: Carmen 16, Fiona 92 |
| 17 |  | Waiter: nothing to do<br>Chef waits | Ethan pays $15.00 (satisfaction 84)<br>Waiting: Carmen 4, Fiona 84 |
| 18 |  | Waiter seats Fiona (Table 1)<br>Chef waits | Waiting: Carmen 0, Fiona 76<br>Carmen leaves (0), Pasta CANCELLED |

Result: VICTORY (4 paid, $55.50, average 83.5) with 1 unhappy departure. Carmen reaches 0 at the
end of turn 18 and leaves without paying. Her Pasta order is CANCELLED, Table 2 is freed and nothing
is added to revenue.

### Rule checks added in Stage 2

`./test.sh` now runs 97 checks. The Stage 1 rule checks still pass, including the Section 3.3
example, abandonment, one task per turn and the payment order. Because the arrivals changed, the
Stage 1 replays and the "all customers are Regular" check became their Stage 2 versions. The new
checks are:

| Group | What is checked |
| --- | --- |
| Customer types | Satisfaction loss per waiting turn: Regular 8, VIP 5, Critic 12; VIP bill 10% off ($15.00 -> $13.50, $9.00 -> $8.10, $18.00 -> $16.20); Regular and Critic pay full price; rounding to the nearest cent |
| Critic preference | A Critic orders Pasta whatever their arrival number; with two equally demanding dishes, the more expensive one |
| Same flow for every type | A VIP and a Critic are seated, order, are cooked for, served, eat and pay through the same controller steps |
| No type checks | `GameController.java` contains no `instanceof`, no `getType()` and no reference to `VIPCustomer` or `CriticCustomer` |
| Arrivals | Still six arrivals, with the VIP on turn 4 and the Critic on turn 10 |
| Replays | The three Stage 2 runs above give the expected results and match their transcripts |
