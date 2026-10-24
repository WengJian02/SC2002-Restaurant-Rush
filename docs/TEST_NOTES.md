# Test notes

The game has no randomness, so the same answers always give the same game and every run below
can be reproduced. Each run is a plain text file of answers, one per line, and any text after `//`
is a note. Replay a run with `--echo` to see each answer printed next to its prompt:

```
./run.sh --echo < runs/stage-3/victory.txt        (Windows: run.bat --echo < runs\stage-3\victory.txt)
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

Replay these runs after `git checkout stage-2`: they use the Stage 2 menu (no combos) and have no Host or Happy Hour questions.

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

At tag `stage-2`, `./test.sh` runs 97 checks. The Stage 1 rule checks still pass, including the
Section 3.3 example, abandonment, one task per turn and the payment order. Because the arrivals
changed, the Stage 1 replays and the "all customers are Regular" check became their Stage 2
versions. The new checks are:

| Group | What is checked |
| --- | --- |
| Customer types | Satisfaction loss per waiting turn: Regular 8, VIP 5, Critic 12; VIP bill 10% off ($15.00 -> $13.50, $9.00 -> $8.10, $18.00 -> $16.20); Regular and Critic pay full price; rounding to the nearest cent |
| Critic preference | A Critic orders Pasta whatever their arrival number; with two equally demanding dishes, the more expensive one |
| Same flow for every type | A VIP and a Critic are seated, order, are cooked for, served, eat and pay through the same controller steps |
| No type checks | `GameController.java` contains no `instanceof`, no `getType()` and no reference to `VIPCustomer` or `CriticCustomer` |
| Arrivals | Still six arrivals, with the VIP on turn 4 and the Critic on turn 10 |
| Replays | The three Stage 2 runs above give the expected results and match their transcripts |

## Stage 3 (tag `stage-3`, final version)

The arrivals are the same as in Stage 2. The menu now also lists three combos, so under the same
food rule customers #5 and #6 order combos, and the Critic's most demanding item is now a combo:

| Customer | Type | Orders | Bill | Units |
| --- | --- | --- | --- | --- |
| #1 Alice | Regular | Salad | $9.00 | 1 |
| #2 Victor | VIP | Burger | $13.50 ($10.80 if ordered during Happy Hour) | 1 |
| #3 Chloe | Regular | Pasta | $18.00 | 2 |
| #4 Carmen | Critic | Burger + Pasta Combo (most units, then highest price) | $29.70 | 3 |
| #5 Ethan | Regular | Salad + Pasta Combo | $24.30 | 3 |
| #6 Fiona | Regular | Burger + Pasta Combo | $29.70 | 3 |

Each turn the game asks about Happy Hour (only while it is unused), then about the Host, the Waiter
and the Chef. A question is skipped when there is nothing to decide, for example the Host's question
when nobody can be seated.

### Victory run: `runs/stage-3/victory.txt`

The Host seats every customer as they arrive, so the Waiter takes the order in the same turn and
the Chef starts at once. Happy Hour is switched on at turn 4 when the VIP orders, so his Burger costs
$15.00 x 0.9 x 0.8 = $10.80 (the brief's example), and on turn 5 he recovers to 100. Turn 13 is the
hardest choice. Ethan's 3-unit combo must start on the turn he arrives or he cannot pay by turn 18,
so the Waiter takes his order before serving Carmen's READY combo.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Host seats Alice (Table 1)<br>Waiter takes Alice's order: Salad, bill $9.00<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 92 |
| 2 |  | Waiter serves Alice's Salad<br>Chef: nothing to do |  |
| 3 |  | Waiter: nothing to do<br>Chef: nothing to do | Alice READY_TO_PAY |
| 4 | #2 Victor (VIP) | Happy Hour ON (this turn and next); waiting customers +15: Victor 100<br>Host seats Victor (Table 2)<br>Waiter takes Victor's order: Burger, bill $10.80 (menu $15.00, Happy Hour 20% off)<br>Chef: Victor's Burger 1/1, READY | Alice pays $9.00 (satisfaction 92)<br>Waiting: Victor 95<br>Happy Hour: 1 turn left |
| 5 |  | Happy Hour turn 2: waiting customers +15: Victor 100<br>Waiter serves Victor's Burger<br>Chef: nothing to do | Happy Hour over |
| 6 |  | Waiter: nothing to do<br>Chef: nothing to do | Victor READY_TO_PAY |
| 7 | #3 Chloe (Regular) | Host seats Chloe (Table 1)<br>Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef: Chloe's Pasta 1/2 | Victor pays $10.80 (satisfaction 100)<br>Waiting: Chloe 92 |
| 8 |  | Waiter: nothing to do<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 84 |
| 9 |  | Waiter serves Chloe's Pasta<br>Chef: nothing to do |  |
| 10 | #4 Carmen (Critic) | Host seats Carmen (Table 2)<br>Waiter takes Carmen's order: Burger + Pasta Combo, bill $29.70<br>Chef: Carmen's Burger + Pasta Combo 1/3 | Chloe READY_TO_PAY<br>Waiting: Carmen 88 |
| 11 |  | Waiter: nothing to do<br>Chef: Carmen's Burger + Pasta Combo 2/3 | Chloe pays $18.00 (satisfaction 84)<br>Waiting: Carmen 76 |
| 12 |  | Waiter: nothing to do<br>Chef: Carmen's Burger + Pasta Combo 3/3, READY | Waiting: Carmen 64 |
| 13 | #5 Ethan (Regular) | Host seats Ethan (Table 1)<br>Waiter takes Ethan's order: Salad + Pasta Combo, bill $24.30<br>Chef: Ethan's Salad + Pasta Combo 1/3 | Waiting: Carmen 52, Ethan 92 |
| 14 |  | Waiter serves Carmen's Burger + Pasta Combo<br>Chef: Ethan's Salad + Pasta Combo 2/3 | Waiting: Ethan 84 |
| 15 |  | Waiter: nothing to do<br>Chef: Ethan's Salad + Pasta Combo 3/3, READY | Carmen READY_TO_PAY<br>Waiting: Ethan 76 |
| 16 | #6 Fiona (Regular) | Waiter serves Ethan's Salad + Pasta Combo<br>Chef: nothing to do | Carmen pays $29.70 (satisfaction 52)<br>Waiting: Fiona 92 |
| 17 |  | Host seats Fiona (Table 2)<br>Waiter takes Fiona's order: Burger + Pasta Combo, bill $29.70<br>Chef: Fiona's Burger + Pasta Combo 1/3 | Ethan READY_TO_PAY<br>Waiting: Fiona 84 |
| 18 |  | Waiter: nothing to do<br>Chef: Fiona's Burger + Pasta Combo 2/3 | Ethan pays $24.30 (satisfaction 76)<br>Waiting: Fiona 76 |

Result: VICTORY. 5 paid, revenue $91.80 (9 + 10.80 + 18 + 29.70 + 24.30), average satisfaction
80.8 = (92 + 100 + 84 + 52 + 76) / 5.

### Same game with Happy Hour used later: `runs/stage-3/happy-hour-late.txt`

This is the victory run except that Happy Hour is saved for turn 13, when Carmen has been waiting
for her combo and Ethan arrives. Both turns of recovery go to customers who are waiting, and Carmen is
served on turn 14 with 82 instead of 52.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Host seats Alice (Table 1)<br>Waiter takes Alice's order: Salad, bill $9.00<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 92 |
| 2 |  | Waiter serves Alice's Salad<br>Chef: nothing to do |  |
| 3 |  | Waiter: nothing to do<br>Chef: nothing to do | Alice READY_TO_PAY |
| 4 | #2 Victor (VIP) | Host seats Victor (Table 2)<br>Waiter takes Victor's order: Burger, bill $13.50 (menu $15.00)<br>Chef: Victor's Burger 1/1, READY | Alice pays $9.00 (satisfaction 92)<br>Waiting: Victor 95 |
| 5 |  | Waiter serves Victor's Burger<br>Chef: nothing to do |  |
| 6 |  | Waiter: nothing to do<br>Chef: nothing to do | Victor READY_TO_PAY |
| 7 | #3 Chloe (Regular) | Host seats Chloe (Table 1)<br>Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef: Chloe's Pasta 1/2 | Victor pays $13.50 (satisfaction 95)<br>Waiting: Chloe 92 |
| 8 |  | Waiter: nothing to do<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 84 |
| 9 |  | Waiter serves Chloe's Pasta<br>Chef: nothing to do |  |
| 10 | #4 Carmen (Critic) | Host seats Carmen (Table 2)<br>Waiter takes Carmen's order: Burger + Pasta Combo, bill $29.70<br>Chef: Carmen's Burger + Pasta Combo 1/3 | Chloe READY_TO_PAY<br>Waiting: Carmen 88 |
| 11 |  | Waiter: nothing to do<br>Chef: Carmen's Burger + Pasta Combo 2/3 | Chloe pays $18.00 (satisfaction 84)<br>Waiting: Carmen 76 |
| 12 |  | Waiter: nothing to do<br>Chef: Carmen's Burger + Pasta Combo 3/3, READY | Waiting: Carmen 64 |
| 13 | #5 Ethan (Regular) | Happy Hour ON (this turn and next); waiting customers +15: Carmen 79, Ethan 100<br>Host seats Ethan (Table 1)<br>Waiter takes Ethan's order: Salad + Pasta Combo, bill $19.44 (menu $24.30, Happy Hour 20% off)<br>Chef: Ethan's Salad + Pasta Combo 1/3 | Waiting: Carmen 67, Ethan 92<br>Happy Hour: 1 turn left |
| 14 |  | Happy Hour turn 2: waiting customers +15: Carmen 82, Ethan 100<br>Waiter serves Carmen's Burger + Pasta Combo<br>Chef: Ethan's Salad + Pasta Combo 2/3 | Waiting: Ethan 92<br>Happy Hour over |
| 15 |  | Waiter: nothing to do<br>Chef: Ethan's Salad + Pasta Combo 3/3, READY | Carmen READY_TO_PAY<br>Waiting: Ethan 84 |
| 16 | #6 Fiona (Regular) | Waiter serves Ethan's Salad + Pasta Combo<br>Chef: nothing to do | Carmen pays $29.70 (satisfaction 82)<br>Waiting: Fiona 92 |
| 17 |  | Host seats Fiona (Table 2)<br>Waiter takes Fiona's order: Burger + Pasta Combo, bill $29.70<br>Chef: Fiona's Burger + Pasta Combo 1/3 | Ethan READY_TO_PAY<br>Waiting: Fiona 84 |
| 18 |  | Waiter: nothing to do<br>Chef: Fiona's Burger + Pasta Combo 2/3 | Ethan pays $19.44 (satisfaction 84)<br>Waiting: Fiona 76 |

Result: VICTORY. 5 paid, revenue $89.64 (9 + 13.50 + 18 + 29.70 + 19.44), average satisfaction
87.4 = (92 + 95 + 84 + 82 + 84) / 5. Compared with the victory run, moving Happy Hour to turn 13 costs
$2.16 of revenue and gains 6.6 points of average satisfaction.

### Defeat run: `runs/stage-3/defeat.txt`

The mistakes: Happy Hour is switched on at turn 1, when nobody needs it, so it only discounts
Alice's Salad. The new Host is never used. The Waiter seats new customers and takes orders before
serving READY dishes, and the Chef is kept waiting on the turn each order is taken.

| Turn | Arrival | Manager's choices and results | End of turn |
| --- | --- | --- | --- |
| 1 | #1 Alice (Regular) | Happy Hour ON (this turn and next); waiting customers +15: Alice 100<br>Host waits<br>Waiter seats Alice (Table 1)<br>Chef: nothing to do | Waiting: Alice 92<br>Happy Hour: 1 turn left |
| 2 |  | Happy Hour turn 2: waiting customers +15: Alice 100<br>Waiter takes Alice's order: Salad, bill $7.20 (menu $9.00, Happy Hour 20% off)<br>Chef waits | Waiting: Alice 92<br>Happy Hour over |
| 3 |  | Waiter: nothing to do<br>Chef: Alice's Salad 1/1, READY | Waiting: Alice 84 |
| 4 | #2 Victor (VIP) | Host waits<br>Waiter seats Victor (Table 2)<br>Chef: nothing to do | Waiting: Alice 76, Victor 95 |
| 5 |  | Waiter takes Victor's order: Burger, bill $13.50 (menu $15.00)<br>Chef waits | Waiting: Alice 68, Victor 90 |
| 6 |  | Waiter serves Alice's Salad<br>Chef: Victor's Burger 1/1, READY | Waiting: Victor 85 |
| 7 | #3 Chloe (Regular) | Waiter serves Victor's Burger<br>Chef: nothing to do | Alice READY_TO_PAY<br>Waiting: Chloe 92 |
| 8 |  | Waiter: nothing to do<br>Chef: nothing to do | Victor READY_TO_PAY<br>Alice pays $7.20 (satisfaction 68)<br>Waiting: Chloe 84 |
| 9 |  | Host waits<br>Waiter seats Chloe (Table 1)<br>Chef: nothing to do | Victor pays $13.50 (satisfaction 85)<br>Waiting: Chloe 76 |
| 10 | #4 Carmen (Critic) | Host waits<br>Waiter seats Carmen (Table 2)<br>Chef: nothing to do | Waiting: Chloe 68, Carmen 88 |
| 11 |  | Waiter takes Chloe's order: Pasta, bill $18.00<br>Chef waits | Waiting: Chloe 60, Carmen 76 |
| 12 |  | Waiter takes Carmen's order: Burger + Pasta Combo, bill $29.70<br>Chef: Chloe's Pasta 1/2 | Waiting: Chloe 52, Carmen 64 |
| 13 | #5 Ethan (Regular) | Waiter: nothing to do<br>Chef: Chloe's Pasta 2/2, READY | Waiting: Chloe 44, Carmen 52, Ethan 92 |
| 14 |  | Waiter serves Chloe's Pasta<br>Chef: Carmen's Burger + Pasta Combo 1/3 | Waiting: Carmen 40, Ethan 84 |
| 15 |  | Waiter: nothing to do<br>Chef: Carmen's Burger + Pasta Combo 2/3 | Chloe READY_TO_PAY<br>Waiting: Carmen 28, Ethan 76 |
| 16 | #6 Fiona (Regular) | Waiter: nothing to do<br>Chef: Carmen's Burger + Pasta Combo 3/3, READY | Chloe pays $18.00 (satisfaction 44)<br>Waiting: Carmen 16, Ethan 68, Fiona 92 |
| 17 |  | Host waits<br>Waiter seats Ethan (Table 1)<br>Chef: nothing to do | Waiting: Carmen 4, Ethan 60, Fiona 84 |
| 18 |  | Waiter takes Ethan's order: Salad + Pasta Combo, bill $24.30<br>Chef waits | Waiting: Carmen 0, Ethan 52, Fiona 76<br>Carmen leaves (0), Burger + Pasta Combo CANCELLED |

Result: DEFEAT. Only 3 customers paid (target 4) and revenue is $38.70 (target $45.00). The average
of those who paid is 65.7 ((68 + 85 + 44) / 3 = 65.67). Carmen's 3-unit combo was READY on turn 16,
but on turn 17 the Waiter seated Ethan instead of serving her. She reached 0 at the end of turn 18 and
left without paying, so her combo was CANCELLED and she counts as an unhappy departure.

### Rule checks added in Stage 3

`./test.sh` now runs 131 checks. The Stage 1 and Stage 2 rule checks still pass on the Stage 3
code, including the Section 3.3 example, abandonment, the payment order, the VIP and Critic behaviour
and the check that the controller has no type checks. The five checks that describe the setting
itself were updated (see below). The new checks are:

| Group | What is checked |
| --- | --- |
| Base setting | Staff now include the Host; the menu lists the three dishes and the three combos with their prices and units |
| ComboMeal | Salad + Pasta = $24.30 and 3 units (brief's example); Salad + Burger $21.60 / 2; Burger + Pasta $29.70 / 3; rounding to the nearest cent; the combo choice rule (customers 4-6 land on combos; a Critic picks Burger + Pasta); a combo is ordered, prepared one unit per turn, served and paid like any dish |
| Host | Host and Waiter are both `Seater`s; the Host seats at most one customer per turn; the Waiter can seat another customer in the same turn; a customer seated by the Host can order in the same turn; the Host follows the same table rule |
| Happy Hour pricing | Only at the start of a turn (refused once a staff member has acted); once per game; lasts two turns (2 turns left when switched on, 1 at the start of the next turn); 20% off orders taken while it is on; orders taken before keep their locked price; orders after pay full price; VIP first, then Happy Hour ($10.80) |
| Happy Hour recovery | +15 for customers waiting for a table or food at activation and again at the start of the second turn; never above 100; served customers and customers who have left are not affected |
| Replays | The three Stage 3 runs above give the expected results and match their transcripts |

Five earlier checks were updated because the setting grew. The staff check now expects the Host,
the base-menu check no longer expects exactly three items, the food-rule check and the check that a
Critic picks Pasta build the brief's three-dish menu themselves, and the input-handling check answers
the new Happy Hour and Host questions first. The rules they test did not change. The Stage 2 runs are
replayed at `stage-2`, because the Stage 3 menu and the Host change those games.
