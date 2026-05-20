import java.util.ArrayList;

public class WildCard extends Card implements DealsDamage, AppliesFreeze {
    private String effectType;
    private int damage;

    public WildCard() {
        int minDamage = 5;
        int maxDamage = 8;
        this.damage = Rand.randomInt(minDamage, maxDamage + 1);


        int minPoints = 1;
        int maxPoints = 3;
        int pointValue = Rand.randomInt(minPoints, maxPoints + 1);
        super(pointValue);


        String[] effects = {"Draw Again", "Skip Card", "Damage All", "Freeze Random Player"};
        this.effectType = effects[Rand.randomInt(0, effects.length)];
    }

    @Override
    public void play(Player currentPlayer, ArrayList<Player> allPlayers) {
        System.out.println("Wildcard played! Effect: " + effectType);

        switch (effectType) {
            case "Draw Again":

                System.out.println(currentPlayer.getName() + " gets a bonus draw!");
                currentPlayer.addCardToHand(new PointCard()); // Or logic to pull from mixedDeck
                break;

            case "Skip Card":
                skipNextPlayer(currentPlayer, allPlayers);
                break;

            case "Damage All":
                for (Player p : allPlayers) {

                    if (p != currentPlayer) {
                        doDamage(currentPlayer, p);
                    }
                }
                break;

            case "Freeze Random Player":
                applyRandomFreeze(currentPlayer, allPlayers);
                break;
        }
    }

    @Override
    public void doDamage(Player currentPlayer, Player playerToDamage) {

        playerToDamage.removePoints(this.damage);

        System.out.println(currentPlayer.getName() + " dealt " + damage + " damage to " + playerToDamage.getName() + ".");
        System.out.println(playerToDamage.getName() + " now has " + playerToDamage.getNumPoints() + " points.");
    }

    @Override
    public void freeze(Player currentPlayer, Player playerToFreeze) {
        playerToFreeze.freeze();
        System.out.println(playerToFreeze.getName() + " is frozen and will miss their next turn!");
    }

    private void applyRandomFreeze(Player current, ArrayList<Player> players) {
        if (players.size() <= 1) return;

        Player target;
        do {
            int index = Rand.randomInt(0, players.size());
            target = players.get(index);
        } while (target == current);

        freeze(current, target);
    }

    private void skipNextPlayer(Player current, ArrayList<Player> players) {

        int currentIndex = players.indexOf(current);

        int nextIndex = (currentIndex + 1) % players.size();

        Player nextPlayer = players.get(nextIndex);
        freeze(current, nextPlayer);
    }

    @Override
    public String toString() {
        return "WildCard [" + effectType + "] (Value: " + getPointValue() + ")";
    }
}