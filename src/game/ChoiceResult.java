package game;

import logic.AlgorithmStep;
import model.Node;

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
        String message = "No! " + about + ", but " + right.getName() + " is only " + rightDist
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
