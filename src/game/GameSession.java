package game;

import java.util.ArrayList;

import model.Node;
import model.Graph;
import logic.AlgorithmStep;

public class GameSession(){
    private Graph graph;
    private ArrayList<Node> stepList;
    private int currentIndex;
    private int strikes;
    private static final int MAX_STRIKES = 3;
    private final ArrayList<Node> finalPath;
    
    public GameSession(Graph graph, ArrayList<Node> finalPath){
        this.graph = graph;
        this.finalPath = finalPath;
        this.stepList = new ArrayList<>();
        this.currentIndex = 0;
        this.strikes = 0;

    }

    public ChoiceResult submitChoice(Node chosen) {
        AlgorithmStep step = steps.get(currentIndex);
        Node right = step.getCorrectNextNode();

        if (chosen.equals(right)) {
            currentIndex++;
            return ChoiceResult.right(right, step.getDistance(right), strikes);
        }
        strikes++;
        return ChoiceResult.wrong(chosen, step.getDistance(chosen),
                                right, step.getDistance(right), strikes, MAX_STRIKES);
    }


}
    
    