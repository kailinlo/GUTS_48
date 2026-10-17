package logic;

import java.util.Map;
import java.util.Set;

/** Snapshot of the algorithm state just BEFORE a node is picked. */
public class AlgorithmStep {

    public static final int INFINITY = Integer.MAX_VALUE;

    private final int stepIndex;                  // starting state = 0
    private final Map<String, Integer> distances; // node id -> current best distance
    private final Map<String, String> previous;   // node id -> previous node id (null if none)
    private final Set<String> visited;            // node ids already visited
    private final String correctNextNode;         // unvisited node with smallest distance (null on the final step)

    public AlgorithmStep(int stepIndex, Map<String, Integer> distances, Map<String, String> previous,
                         Set<String> visited, String correctNextNode) {
        this.stepIndex = stepIndex;
        this.distances = distances;
        this.previous = previous;
        this.visited = visited;
        this.correctNextNode = correctNextNode;
    }

    public int getStepIndex()                  { return stepIndex; }
    public Map<String, Integer> getDistances() { return distances; }
    public Map<String, String> getPrevious()   { return previous; }
    public Set<String> getVisited()            { return visited; }
    public String getCorrectNextNode()         { return correctNextNode; }
    public int getDistance(String nodeId)      { return distances.get(nodeId); }
}
