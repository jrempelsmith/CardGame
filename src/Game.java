import java.util.ArrayList;

public class Game {

    // ----------- Settings ----------- //

    // Player settings
    private int startingHandSize;

    private float playerChancesOfPlayingCard; // % chance (0-1) that a player plays a card from their hand
    private float playerChancesOfDrawingFromMixedDeck; // % chance (0-1) that a player draws from the mixed deck
    // private float playerChancesOfDrawingFromDamageDeck; // damage deck chances are the leftovers of the other chances

    // Deck settings
    private int totalNumberOfCards;
    private float pointCardChances; // % chance (from 0-1) of generating a point card
    private float attackCardChances; // % chance (from 0-1) of generating an attack card
    private float freezeCardChances;// % chance (from 0-1) of generating a freeze card
    private float wildCardChances;
    //private float thiefCardChances; // thief card chances are the leftovers of the other chances

    private float chancesOfDamageCardBeingInDamageDeck; // % chance of a generated damage card being added to the damage-only deck

    // -------- End of Settings ------- //


    // --------- Game Objects --------- //

    private ArrayList<Player> players;
    private ArrayList<Card> mixedDeck; // contains a mix of all types of cards
    private ArrayList<DealsDamage> damageDeck; // contains only cards that implement DealsDamage

    private ArrayList<Player> team1;
    private ArrayList<Player> team2;

    // ------ End of Game Objects ----- //



    // Starts up game
    public Game() {
        // Set game settings
        setGameSettings();

        // Game objects
        players = new ArrayList<Player>();
        mixedDeck = new ArrayList<Card>();
        damageDeck = new ArrayList<DealsDamage>();
        team1 = new ArrayList<Player>();
        team2 = new ArrayList<Player>();

        // Generate the decks
        generateDecks();
    }

    public void registerPlayer(Player player) {
        players.add(player);
    }

    public void run() {

        // Display Players and Teams
        int index1 = Rand.randomInt(0, 4);
        int index2 = Rand.randomInt(0, 4);
        if (index1 != index2) {
            team1.add(players.get(index1));
            team1.add(players.get(index2));

            for (int i = 0; i < players.size(); i++) {
                if (i != index1 && i != index2){
                    team2.add(players.get(i));
                }
            }
        }


        System.out.println("");
        System.out.println("Players: ");
        for (int i = 0; i < players.size(); i++) {
            Player hold = players.get(i);
            System.out.println(hold.getName());
        }

        System.out.println("");
        System.out.println("Team 1: ");
        for (int i = 0; i < team1.size(); i++) {
            Player hold = team1.get(i);
            System.out.println(hold.getName());
        }

        System.out.println("");
        System.out.println("Team 2: ");
        for (int i = 0; i < team2.size(); i++) {
            Player hold = team2.get(i);
            System.out.println(hold.getName());
        }


        // deal cards to each player
        int cardsAdded = 0;
        while (cardsAdded < startingHandSize) {
            for (int i = 0; i < players.size(); i++) {
                int randomCardIndex = Rand.randomInt(0, mixedDeck.size());
                Card randomCard = mixedDeck.get(randomCardIndex);
                mixedDeck.remove(randomCardIndex);
                players.get(i).addCardToHand(randomCard);
            }
            cardsAdded += 1;
        }

        int currentPlayerIndex = -1; // will increase to 0 when the loop starts
        Player currentPlayer;

        // game loop -- loop as long as either deck has cards

        while (mixedDeck.size() > 0 || damageDeck.size() > 0) {

            // switch to next player
            currentPlayerIndex += 1;
            if (currentPlayerIndex >= players.size()) {
                currentPlayerIndex = 0;
            }
            currentPlayer = players.get(currentPlayerIndex);

            System.out.println("\n# cards remaining in Mixed deck: " + mixedDeck.size() + ".");
            System.out.println("# cards remaining in Damage deck: " + damageDeck.size() + ".\n");

            System.out.println("It's " + currentPlayer.getName() + "'s turn.\n");
            currentPlayer.displayStatus();
            Input.waitForUserToPressEnter("\nPress Enter to play " + currentPlayer.getName() + "'s turn.");

            // check if the player should be skipped
            if (currentPlayer.isFrozen()) {
                System.out.println(currentPlayer.getName() + " is frozen! Skipping turn.");
                currentPlayer.unfreeze();
                continue; // skips the rest of the body of the loop, and returns to the start of the loop
            }

            // generate a random value to choose a random action
            float randomValue = Rand.random();

            // 1. play a card from player's hand
            if (randomValue < playerChancesOfPlayingCard && currentPlayer.hasCardsInHand()) {
                currentPlayer.playRandomCardFromHand(players);
            }

            // 2. OR draw a card from mixed deck (but don't play it yet)
            else if (damageDeck.size() == 0 || (mixedDeck.size() > 0 && randomValue < playerChancesOfPlayingCard + playerChancesOfDrawingFromMixedDeck)) {
                Object drawnObject = drawRandomCard(mixedDeck);
                Card drawnCard = (Card)drawnObject;
                currentPlayer.addCardToHand(drawnCard);

                System.out.println(currentPlayer.getName() + " drew a " + drawnCard + " from the Mixed deck.");
            }

            // 3. OR draw a card from damage deck and use its damage effect immediately, without getting points
            else {
                Object drawnObject = drawRandomCard(damageDeck);
                DealsDamage damageCard = (DealsDamage)drawnObject;

                System.out.println(currentPlayer.getName() + " drew a " + damageCard + " from the Damage deck.");

                if (damageCard instanceof WildCard) {
                    WildCard wildCard = (WildCard) damageCard;
                    wildCard.play(currentPlayer, players);

                }
                    // pick a random player (but not oneself) to apply the damage card to
                else {
                    boolean selectedAnotherPlayer = false;
                    Player otherPlayer = null;



                while (!selectedAnotherPlayer) {
                    int randomPlayerIndex = Rand.randomInt(0, players.size());
                    otherPlayer = players.get(randomPlayerIndex);
                    if (otherPlayer != currentPlayer) {
                        selectedAnotherPlayer = true;
                    }
                }

                damageCard.doDamage(currentPlayer, otherPlayer);
                if (damageCard instanceof AppliesFreeze) {
                    AppliesFreeze freezeCard = (AppliesFreeze)damageCard;
                    freezeCard.freeze(currentPlayer, otherPlayer);
                }
            }

            Input.waitForUserToPressEnter("\nPress Enter to end " + currentPlayer.getName() + "'s turn.\n");
            System.out.println("");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
        }

        // End game: determine which Player had the most points
        declareWinner();
    }

    // Randomly selects a reference (Card or DealsDamage) from an ArrayList (mixedDeck or damageDeck).
    // Removes the randomly selected reference from the specified ArrayList.
    // Returns the selected reference as an Object (because we don't know what type the ArrayList stores).
    private Object drawRandomCard(ArrayList arrayList) {
        int randomCardIndex = Rand.randomInt(0, arrayList.size());
        Object randomCard = arrayList.remove(randomCardIndex);
        return randomCard;
    }

    // Initializes the settings fields.
    private void setGameSettings() {
        // Player settings
        startingHandSize = 3;
        playerChancesOfPlayingCard = 0.5f; // 50% play card, 25% draw card from mixed, 25% draw card from damage deck and play immediately
        playerChancesOfDrawingFromMixedDeck = 0.25f;
        float playerChancesOfDrawingFromDamageDeck = 1f - (playerChancesOfPlayingCard + playerChancesOfDrawingFromMixedDeck);
        if (playerChancesOfDrawingFromDamageDeck < 0f) {
            System.out.println("ERROR: Chances of different player actions are not all positive.");
        }


        // Deck settings
        totalNumberOfCards = 20;
        chancesOfDamageCardBeingInDamageDeck = 0.4f;

        pointCardChances = 0.5f; // must be between 0 and 1
        attackCardChances = 0.25f; // must be between 0 and 1
        freezeCardChances = 0.15f; // must be between 0 and 1
        wildCardChances = 0.1f; // must be between 0 and 1

        // thief card chances should be positive based on the math, but check just to be safe
        float thiefCardChances = 1f - (pointCardChances + attackCardChances + freezeCardChances + wildCardChances);
        if (thiefCardChances < 0f) {
            System.out.println("ERROR: Card chances are not all positive.");
        }
    }

    // Populates the two ArrayLists with random Cards, according to the settings.
    private void generateDecks() {
        for (int i = 0; i < totalNumberOfCards; i++) {

            float randomValue = Rand.random(); // 0.0 -> 0.999...

            // % chance of creating a point card
            if (randomValue < pointCardChances) {
                mixedDeck.add(new PointCard());
            }

            // % chance of creating an attack card
            else if (randomValue < pointCardChances + attackCardChances) {
                AttackCard newAttackCard = new AttackCard();

                if (Rand.random() < chancesOfDamageCardBeingInDamageDeck) {
                    damageDeck.add(newAttackCard);
                } else {
                    mixedDeck.add(newAttackCard);
                }
            }

            // % chance of creating a wild card
            else if (randomValue < pointCardChances + attackCardChances + freezeCardChances + wildCardChances) {
                WildCard newWildCard = new WildCard();

                // Because WildCard implements DealsDamage, it can legally be assigned to the damage deck!
                if (Rand.random() < chancesOfDamageCardBeingInDamageDeck) {
                    damageDeck.add(newWildCard);
                } else {
                    mixedDeck.add(newWildCard);
                }
            }

            // % chance of creating a freeze card
            else if (randomValue < pointCardChances + attackCardChances + freezeCardChances) {
                FreezeCard newFreezeCard = new FreezeCard();

                if (Rand.random() < chancesOfDamageCardBeingInDamageDeck) {
                    damageDeck.add(newFreezeCard);
                } else {
                    mixedDeck.add(newFreezeCard);
                }
            }

            // % chance of creating a thief card
            else {
                mixedDeck.add(new ThiefCard());
            }
        }
    }

    private void declareWinner() {
        int highestScore = 0;
        Player playerWithHighestScore = null;

        System.out.println("\nFinal Scoreboard:");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            System.out.println(p.getName() + ": " + p.getNumPoints());

            // update highest score tracker
            if (p.getNumPoints() >= highestScore) {
                highestScore = p.getNumPoints();
                playerWithHighestScore = p;
            }
        }

        System.out.println("Player '" + playerWithHighestScore.getName() + "' wins!");
    }
}
