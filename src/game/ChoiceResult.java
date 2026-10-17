package game;
import model.Node;

public class ChoiceResult{

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

    public static ChoiceResult right(Node node, int distance, int strikes){
        String message = "Correct! " + node.getName() + " has the smallest distance so far (" + distance + "km), so you should look at it next.";
        return new ChoiceResult(true, node, node, message, strikes, false);
    }

    public static ChoiceResult wrong(Node chosen, int chosenDist,
                                     Node right, int rightDist,
                                     int strikes, int maxStrikes) {
        String message = "No. you are at " + chosenDist
                + " km, but " + right.getName() + " is only " + rightDist
                + " km. You should always visit the closest unvisited node first.";
        return new ChoiceResult(false, chosen, right, message, strikes, strikes >= maxStrikes);
    }

    public boolean isCorrect(){
        return correct;
    }

    public Node getChosenNode(){
        return chosenNode;
    }

    public Node getRightNode(){
        return rightNode;
    }

    public String getExplanation(){
        return explanation;
    }
    
    public int getStrikes(){
        return strikes;
    }
    
    public boolean isGameOver(){
        return gameOver;
    }

}