package restaurant;

import java.util.Scanner;

import restaurant.boundary.RestaurantCLI;
import restaurant.control.GameController;
import restaurant.control.GameSetup;

public class Main {

    public static void main(String[] args) {
        // --echo prints each answer after its prompt (used when the answers come from a file)
        boolean echo = args.length > 0 && args[0].equals("--echo");
        GameController game = GameSetup.createBaseGame();
        RestaurantCLI cli = new RestaurantCLI(game, new Scanner(System.in), echo);
        cli.play();
    }
}
