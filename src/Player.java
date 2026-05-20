import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Card> hand;
    private int numPoints;
    private boolean isFrozen;
    private String team; // "team1" or "team2"

    public Player(String name) {
        this.name = name;
        hand = new ArrayList<Card>();
        numPoints = 5;
        isFrozen = false;
    }

    public void playRandomCardFromHand(ArrayList<Player> players) {
        // select a random card from our hand to play
        int randomCardIndex = Rand.randomInt(0, hand.size());
        Card randomCard = hand.remove(randomCardIndex);
        randomCard.play(this, players);

        // pick a random player on the opposing team to apply any additional actions to
        Player target = chooseTarget(players);

        // do possible additional damage action
        if (randomCard instanceof DealsDamage) {
            DealsDamage damageCard = (DealsDamage) randomCard;
            damageCard.doDamage(this, target);
        }

        // do possible additional freeze action
        if (randomCard instanceof AppliesFreeze) {
            AppliesFreeze freezeCard = (AppliesFreeze) randomCard;
            freezeCard.freeze(this, target);
        }
    }

    // Choose a random target from the opposing team only
    public Player chooseTarget(ArrayList<Player> players) {
        boolean selectedAnotherPlayer = false;
        Player otherPlayer = null;

        while (!selectedAnotherPlayer) {
            int randomPlayerIndex = Rand.randomInt(0, players.size());
            otherPlayer = players.get(randomPlayerIndex);
            // only target players on the opposing team
            if (otherPlayer != this && !otherPlayer.getTeam().equals(this.team)) {
                selectedAnotherPlayer = true;
            }
        }
        return otherPlayer;
    }

    public boolean hasCardsInHand() {
        return hand.size() > 0;
    }

    public void addCardToHand(Card card) {
        hand.add(card);
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void freeze() {
        isFrozen = true;
    }

    public void unfreeze() {
        isFrozen = false;
    }

    public Card removeRandomCard() {
        if (hand.size() == 0) {
            return null; // returning null indicates there are no cards to remove
        }

        int randomCardIndex = Rand.randomInt(0, hand.size());
        return hand.remove(randomCardIndex); // ArrayList.remove both removes AND returns a reference to the object
    }

    public String getName() {
        return name;
    }

    // Assigns this player to a team
    public void setTeam(String team) {
        this.team = team;
    }

    public String getTeam() {
        return team;
    }

    public void addPoints(int pointsToAdd) {
        numPoints += pointsToAdd;
        if (numPoints < 0) {
            numPoints = 0;
        }
    }

    public void removePoints(int pointsToRemove) {
        addPoints(-pointsToRemove);
    }

    public int getNumPoints() {
        return numPoints;
    }

    public void displayStatus() {
        System.out.println(" | ----- " + name + " ----- ");
        System.out.println(" | Team: " + team);
        System.out.println(" | Points: " + numPoints);
        if (isFrozen) {
            System.out.println(" | *FROZEN*");
        }
        System.out.println(" | Cards in hand:");
        for (int i = 0; i < hand.size(); i++) {
            System.out.print(" | " + (i + 1) + ": ");
            System.out.println(hand.get(i));
        }
        System.out.println(" | ----- ----- ----- ");
    }
}