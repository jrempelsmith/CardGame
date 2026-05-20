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


    // Constructor -- initializes settings and all game object lists, then generates the decks
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

    // Adds a player to the game's player list
    public void registerPlayer(Player player) {
        players.add(player);
    }

    // Sets the two teams using players chosen in Main, and assigns each player their team label
    public void setTeams(Player t1p1, Player t1p2, Player t2p1, Player t2p2) {
        t1p1.setTeam("team1");
        t1p2.setTeam("team1");
        t2p1.setTeam("team2");
        t2p2.setTeam("team2");
        team1.add(t1p1);
        team1.add(t1p2);
        team2.add(t2p1);
        team2.add(t2p2);
    }

    public void run() {

        // Display all players and their team assignments
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

        // Deal starting cards to each player from the mixed deck
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

        // Main game loop -- continues until both decks are empty
        while (mixedDeck.size() > 0 || damageDeck.size() > 0) {

            // Advance to the next player, wrapping around to the start if needed
            currentPlayerIndex += 1;
            if (currentPlayerIndex >= players.size()) {
                currentPlayerIndex = 0;
            }
            currentPlayer = players.get(currentPlayerIndex);

            // Show how many cards are left in each deck
            System.out.println("\n# cards remaining in Mixed deck: " + mixedDeck.size() + ".");
            System.out.println("# cards remaining in Damage deck: " + damageDeck.size() + ".\n");

            // Display the current player's status and wait for input before starting their turn
            System.out.println("It's " + currentPlayer.getName() + "'s turn.\n");
            currentPlayer.displayStatus();
            Input.waitForUserToPressEnter("\nPress Enter to play " + currentPlayer.getName() + "'s turn.");

            // If the player is frozen, skip their turn and unfreeze them
            if (currentPlayer.isFrozen()) {
                System.out.println(currentPlayer.getName() + " is frozen! Skipping turn.");
                currentPlayer.unfreeze();
                continue; // skips the rest of the body of the loop, and returns to the start of the loop
            }

            // Human player gets to manually choose their action
            if (currentPlayer instanceof HumanPlayer) {
                System.out.println("Choose an action:");
                System.out.println("1: Play a card from your hand");
                System.out.println("2: Draw from mixed deck");
                System.out.println("3: Draw from damage deck");
                int action = Input.getUserInt("Enter a number: ");

                if (action == 1) {
                    // Play a card from hand
                    currentPlayer.playRandomCardFromHand(players);
                } else if (action == 2) {
                    // Draw from the mixed deck and add it to hand (don't play it)
                    Object drawnObject = drawRandomCard(mixedDeck);
                    Card drawnCard = (Card) drawnObject;
                    currentPlayer.addCardToHand(drawnCard);
                    System.out.println(currentPlayer.getName() + " drew a " + drawnCard + " from the Mixed deck.");
                } else if (action == 3) {
                    // Draw from the damage deck and immediately apply its damage effect
                    Object drawnObject = drawRandomCard(damageDeck);
                    DealsDamage damageCard = (DealsDamage) drawnObject;
                    System.out.println(currentPlayer.getName() + " drew a " + damageCard + " from the Damage deck.");
                    Player otherPlayer = currentPlayer.chooseTarget(players);
                    damageCard.doDamage(currentPlayer, otherPlayer);

                    // If the damage card also applies a freeze, apply that too
                    if (damageCard instanceof AppliesFreeze) {
                        AppliesFreeze freezeCard = (AppliesFreeze) damageCard;
                        freezeCard.freeze(currentPlayer, otherPlayer);
                    }
                }

            } else {
                // AI player -- generate a random value to choose a random action
                float randomValue = Rand.random();

                // 1. play a card from player's hand
                if (randomValue < playerChancesOfPlayingCard && currentPlayer.hasCardsInHand()) {
                    currentPlayer.playRandomCardFromHand(players);
                }

                // 2. OR draw a card from mixed deck (but don't play it yet)
                else if (damageDeck.size() == 0 || (mixedDeck.size() > 0 && randomValue < playerChancesOfPlayingCard + playerChancesOfDrawingFromMixedDeck)) {
                    Object drawnObject = drawRandomCard(mixedDeck);
                    Card drawnCard = (Card) drawnObject;
                    currentPlayer.addCardToHand(drawnCard);
                    System.out.println(currentPlayer.getName() + " drew a " + drawnCard + " from the Mixed deck.");
                }

                // 3. OR draw a card from damage deck and use its damage effect immediately, without getting points
                else {
                    Object drawnObject = drawRandomCard(damageDeck);
                    DealsDamage damageCard = (DealsDamage) drawnObject;

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
                            AppliesFreeze freezeCard = (AppliesFreeze) damageCard;
                            freezeCard.freeze(currentPlayer, otherPlayer);
                        }
                    }
                }
            }

            Input.waitForUserToPressEnter("\nPress Enter to end " + currentPlayer.getName() + "'s turn.\n");
            System.out.println("");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
        }


        // All decks are empty -- end the game and declare a winner
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

                // add to damage deck or mixed deck based on chance
                if (Rand.random() < chancesOfDamageCardBeingInDamageDeck) {
                    damageDeck.add(newAttackCard);
                } else {
                    mixedDeck.add(newAttackCard);
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
                // add to damage deck or mixed deck based on chance

            // % chance of creating a thief card
            else {
                mixedDeck.add(new ThiefCard());
            }
        }
    }

    // Tallies up each team's points and announces the winning team
    private void declareWinner() {
        int team1Score = 0;
        int team2Score = 0;
        System.out.println("\nFinal Scoreboard: ");
        System.out.println("Team 1: ");
        for (int i = 0; i < team1.size(); i++) {
            Player p = team1.get(i);
            System.out.println(p.getName() + " - " + p.getNumPoints());
            team1Score += p.getNumPoints();
        }

        System.out.println("Team 2: ");
        for (int i = 0; i < team2.size(); i++) {
            Player p = team2.get(i);
            System.out.println(p.getName() + " - " + p.getNumPoints());
            team2Score += p.getNumPoints();
        }

        // Announce the team with the higher total score as the winner
        if (team1Score < team2Score) {
            System.out.println("Team 2 Wins with " + team2Score + " Points!");
        } else {
            System.out.println("Team 1 Wins with " + team1Score + " Points!");
        }
    }
}