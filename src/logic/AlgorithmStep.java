package logic;

import java.util.Map;
import java.util.Set;
import java.util.List;

public class AlgorithmStep {

    public static final int INFINITY = Integer.MAX_VALUE;

    private final int stepIndex;                       // starting state = 0
    private final Map<String, Integer> distances;      // node mapped to current distance 
    private final Map<String, String> previous;        // node mapped to previous node (null node if no previous)
    private final Set<String> visited;                 // nodes already visited
    private final String correctNextNode;              // unvisited node with smallest distance


    public AlgorithmStep(int stepIndex, Map<String, Integer> distances, Map<String, String> previous, Set<String> visited, String correctNextNode) {
        this.stepIndex = stepIndex;

        this.distances = distances;
        this.previous = previous;
        this.visited = visited;
        this.correctNextNode = correctNextNode;
    }

    public int getStepIndex() { return stepIndex; }
    public Map<String, Integer> getDistances() { return distances; }
    public Map<String, String> getPrevious() { return previous; }
    public Set<String> getVisited() { return visited; }
    public String getCorrectNextNode() { return correctNextNode; }

}