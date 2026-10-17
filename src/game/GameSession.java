package game;

import logic.AlgorithmStep;
import logic.Dijkstra;
import model.Edge;
import model.Graph;
import model.Node;

import java.util.List;

/** The rules of the game: holds the solved algorithm, the current step and the strikes. */
public class GameSession {
    public static final int MAX_STRIKES = 3;

    private final Graph graph;
    private final Dijkstra dijkstra;
    private final List<AlgorithmStep> steps;
    private int currentIndex = 0;
    private int strikes = 0;
    private GameState state = GameState.CHOOSING;

    public GameSession(Graph graph, Dijkstra dijkstra) {
        this.graph = graph;
        this.dijkstra = dijkstra;
        this.steps = dijkstra.getSteps();
    }

    public AlgorithmStep getCurrentStep() { return steps.get(currentIndex); }
    public GameState getState()           { return state; }
    public int getStrikes()               { return strikes; }

    /** Player clicked a node during the "pick the next node" phase. */
    public ChoiceResult submitChoice(Node chosen) {
        AlgorithmStep step = steps.get(currentIndex);
        Node right = graph.getNode(step.getCorrectNextNode());

        if (chosen.equals(right)) {
            currentIndex++;
            if (steps.get(currentIndex).getCorrectNextNode() == null) state = GameState.FINAL_PATH;
            return ChoiceResult.right(right, step.getDistance(right.getId()), strikes);
        }
        strikes++;
        if (strikes >= MAX_STRIKES) state = GameState.LOST;
        return ChoiceResult.wrong(chosen, step.getDistance(chosen.getId()),
                right, step.getDistance(right.getId()), strikes, MAX_STRIKES);
    }

    /** Player submitted a full route (node ids, Lecture first). */
    public ChoiceResult submitFinalPath(List<String> path) {
        List<String> shortest = dijkstra.getShortestPath();
        if (path.equals(shortest)) {
            state = GameState.WALKING;
            return ChoiceResult.finalRight(dijkstra.getTotalDistance(), strikes);
        }
        strikes++;
        if (strikes >= MAX_STRIKES) state = GameState.LOST;

        String message;
        if (!path.get(path.size() - 1).equals(shortest.get(shortest.size() - 1))) {
            message = "That route doesn't reach Home! Keep clicking nodes until you get there.";
        } else {
            message = "Not the shortest! Your route takes " + pathCost(path) + " min, but a quicker one takes "
                    + dijkstra.getTotalDistance() + " min. Trace back from Home using the 'previous node' column.";
        }
        return ChoiceResult.finalWrong(message, strikes, MAX_STRIKES);
    }

    public void finishWalk() { state = GameState.WON; }

    public int pathCost(List<String> path) {
        int total = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            Edge e = graph.getEdge(path.get(i), path.get(i + 1));
            if (e != null) total += e.getWeight();
        }
        return total;
    }

    /** Plain-English log of what the algorithm just did after a correct pick. */
    public String describeLastUpdate() {
        if (currentIndex == 0) return "";
        AlgorithmStep before = steps.get(currentIndex - 1);
        AlgorithmStep after = steps.get(currentIndex);
        String pickedId = before.getCorrectNextNode();

        StringBuilder sb = new StringBuilder();
        sb.append("Step ").append(currentIndex).append(": visited ")
          .append(graph.getNode(pickedId).getName()).append(" (")
          .append(before.getDistance(pickedId)).append(" min).\n");

        boolean any = false;
        for (Node n : graph.getNodes()) {
            int o = before.getDistance(n.getId());
            int nw = after.getDistance(n.getId());
            if (o != nw) {
                any = true;
                sb.append("  ").append(n.getName()).append(": ")
                  .append(o == AlgorithmStep.INFINITY ? "\u221e" : String.valueOf(o)).append(" \u2192 ").append(nw)
                  .append(o == AlgorithmStep.INFINITY ? " (first route found)" : " (shorter route!)")
                  .append("\n");
            }
        }
        if (!any) sb.append("  No distances improved.\n");
        return sb.toString();
    }
}
