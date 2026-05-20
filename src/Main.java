public class Main {
    public static void main(String[] args) {

        // Welcome message
        System.out.println("=== Welcome to the Card Game ===\n");

        // Prompt the user to enter names for all 4 players
        System.out.println("Enter the names for 4 players:\n");
        String name1 = Input.getUserString("Player 1 name: ");
        String name2 = Input.getUserString("Player 2 name: ");
        String name3 = Input.getUserString("Player 3 name: ");
        String name4 = Input.getUserString("Player 4 name: ");

        // Ask which player the human wants to control
        System.out.println("\nWhich player do YOU want to control?");
        System.out.println("1: " + name1);
        System.out.println("2: " + name2);
        System.out.println("3: " + name3);
        System.out.println("4: " + name4);
        int choice = Input.getUserInt("Enter a number (1-4): ");

        // Create players -- the chosen one is a HumanPlayer, the rest are AI Players
        Player p1 = (choice == 1) ? new HumanPlayer(name1) : new Player(name1);
        Player p2 = (choice == 2) ? new HumanPlayer(name2) : new Player(name2);
        Player p3 = (choice == 3) ? new HumanPlayer(name3) : new Player(name3);
        Player p4 = (choice == 4) ? new HumanPlayer(name4) : new Player(name4);

        // Store all players and names in arrays for easy access during team selection
        Player[] allPlayers = {p1, p2, p3, p4};
        String[] allNames = {name1, name2, name3, name4};

        // Prompt the user to pick 2 players for Team 1
        System.out.println("\n=== Assign Teams ===");
        System.out.println("Pick 2 players for Team 1. The other 2 will be Team 2.\n");

        // Choose the first Team 1 player
        System.out.println("Choose first player for Team 1:");
        for (int i = 0; i < 4; i++) {
            System.out.println((i + 1) + ": " + allNames[i]);
        }
        int t1p1choice = Input.getUserInt("Enter a number (1-4): ") - 1;

        // Choose the second Team 1 player (hides the already-chosen player)
        System.out.println("\nChoose second player for Team 1:");
        for (int i = 0; i < 4; i++) {
            if (i != t1p1choice) {
                System.out.println((i + 1) + ": " + allNames[i]);
            }
        }
        int t1p2choice = Input.getUserInt("Enter a number (1-4): ") - 1;

        // Assign Team 1 players based on choices
        Player team1p1 = allPlayers[t1p1choice];
        Player team1p2 = allPlayers[t1p2choice];
        Player team2p1 = null;
        Player team2p2 = null;

        // Automatically assign the remaining 2 players to Team 2
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (i != t1p1choice && i != t1p2choice) {
                if (count == 0) team2p1 = allPlayers[i];
                else team2p2 = allPlayers[i];
                count++;
            }
        }

        // Display the final team assignments
        System.out.println("\nTeam 1: " + team1p1.getName() + " & " + team1p2.getName());
        System.out.println("Team 2: " + team2p1.getName() + " & " + team2p2.getName());

        // Create the game, register all players, set teams, and start
        Game game = new Game();
        game.registerPlayer(p1);
        game.registerPlayer(p2);
        game.registerPlayer(p3);
        game.registerPlayer(p4);
        game.setTeams(team1p1, team1p2, team2p1, team2p2);
        game.run();
    }
}