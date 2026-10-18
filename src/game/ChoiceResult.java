package game;

import logic.AlgorithmStep;
import model.Node;

import java.util.Map;

/** What GameSession tells the controller after the player does something. */
public class ChoiceResult {

    private final boolean correct;
    private final Node chosenNode;
    private final Node rightNode;
    private final String explanation;
    private final int strikes;
    private final boolean gameOver;

    private ChoiceResult(boolean correct, Node chosenNode, Node rightNode,
                         String explanation, int strikes, boolean gameOver) {
        this.correct = correct;
        this.chosenNode = chosenNode;
        this.rightNode = rightNode;
        this.explanation = explanation;
        this.strikes = strikes;
        this.gameOver = gameOver;
    }

    // EDITED: a funny message for each place the player can wrongly click (keyed by node id).
    // Nodes not listed here (e.g. Home) fall back to a plain "No!".
    private static final Map<String, String> WRONG_FLAVOUR = Map.of(
        "boyd",   "You have walked into the wrong computing lab, oops! You start sweating from embarrassment.",
        "clyde",  "You fell into the River Clyde, the water is pretty gross and your clothes are soaked!",
        "ovo",    "You get caught in a sweaty mosh pit and you forgot to pack deodorant, not good...",
        "wunder", "A local just vomited his Buckfast on you, ew!"
    );

    private static String dist(int d) {
        return d == AlgorithmStep.INFINITY ? "\u221e" : d + " min";
    }

    public static ChoiceResult right(Node node, int distance, int strikes) {
        String message = "Correct! " + node.getName() + " has the smallest distance so far ("
                + dist(distance) + "), so Dijkstra visits it next.";
        return new ChoiceResult(true, node, node, message, strikes, false);
    }

    public static ChoiceResult wrong(Node chosen, int chosenDist, Node right, int rightDist,
                                     int strikes, int maxStrikes) {
        String about = chosenDist == AlgorithmStep.INFINITY
                ? chosen.getName() + " hasn't been reached yet (distance \u221e)"
                : chosen.getName() + " is " + chosenDist + " min away";
        // EDITED: place-specific message first, then the explanation of the right choice
        String flavour = WRONG_FLAVOUR.get(chosen.getId());
        String message = (flavour != null ? flavour + "\n" : "No! ")
                + about + ", but " + right.getName() + " is only " + rightDist
                + " min away. Dijkstra always visits the CLOSEST unvisited node first.";
        return new ChoiceResult(false, chosen, right, message, strikes, strikes >= maxStrikes);
    }

    public static ChoiceResult finalRight(int minutes, int strikes) {
        String message = "Perfect! That's the quickest route: " + minutes
                + " minutes. Time for a shower!";
        return new ChoiceResult(true, null, null, message, strikes, false);
    }

    public static ChoiceResult finalWrong(String message, int strikes, int maxStrikes) {
        return new ChoiceResult(false, null, null, message, strikes, strikes >= maxStrikes);
    }

    public boolean isCorrect()      { return correct; }
    public Node getChosenNode()     { return chosenNode; }
    public Node getRightNode()      { return rightNode; }
    public String getExplanation()  { return explanation; }
    public int getStrikes()         { return strikes; }
    public boolean isGameOver()     { return gameOver; }
}

