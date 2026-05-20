import java.util.ArrayList;

public class HumanPlayer extends Player {

    public HumanPlayer(String name) {
        super(name);
    }

    @Override
    // Only shows and allows targeting players on the opposing team
    public Player chooseTarget(ArrayList<Player> players) {
        int count = 1;
        for (int i = 0; i < players.size(); i++) {
            // only show players on the opposing team
            if (players.get(i) != this && !players.get(i).getTeam().equals(this.getTeam())) {
                System.out.println(count + ": " + players.get(i).getName());
                count++;
            }
        }

        int choice = Input.getUserInt("Choose a target (enter a number): ");
        int count2 = 1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i) != this && !players.get(i).getTeam().equals(this.getTeam())) {
                if (count2 == choice) {
                    return players.get(i);
                }
                count2++;
            }
        }
        return null;
    }
}