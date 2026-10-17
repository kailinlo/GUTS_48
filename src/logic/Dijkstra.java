package logic;

import model.Edge;
import model.Graph;
import model.Node;

import java.util.*;

/**
 * Runs Dijkstra once on the graph's current weights and records an AlgorithmStep before
 * every pick. The last recorded step is a "finished" snapshot whose correctNextNode is null.
 */
public class Dijkstra {
    private final Graph graph;
    private final String startId;
    private final String endId;

    private final List<AlgorithmStep> steps = new ArrayList<>();
    private List<String> shortestPath = new ArrayList<>();
    private int totalDistance;
    private boolean ties;        // true if the answer was ever ambiguous
    private int improvements;    // times a known distance was replaced by a shorter one

    public Dijkstra(Graph graph, String startId, String endId) {
        this.graph = graph;
        this.startId = startId;
        this.endId = endId;
    }

    public void run() {
        steps.clear();
        shortestPath = new ArrayList<>();
        ties = false;
        improvements = 0;

        final int INF = AlgorithmStep.INFINITY;
        Map<String, Integer> dist = new LinkedHashMap<>();
        Map<String, String> prev = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        for (Node n : graph.getNodes()) {
            dist.put(n.getId(), INF);
            prev.put(n.getId(), null);
        }
        dist.put(startId, 0);

        int index = 0;
        while (visited.size() < graph.getNodes().size()) {
            String next = null;
            int best = INF;
            for (Node n : graph.getNodes()) {
                int d = dist.get(n.getId());
                if (!visited.contains(n.getId()) && d < best) {
                    best = d;
                    next = n.getId();
                }
            }
            if (next == null) break;

            int sameCount = 0;
            for (Node n : graph.getNodes()) {
                if (!visited.contains(n.getId()) && dist.get(n.getId()) == best) sameCount++;
            }
            if (sameCount > 1) ties = true;

            steps.add(new AlgorithmStep(index++, new LinkedHashMap<>(dist), new LinkedHashMap<>(prev),
                    new LinkedHashSet<>(visited), next));
            visited.add(next);

            for (Edge e : graph.getEdges(next)) {
                String other = graph.getOtherNode(e, next).getId();
                if (visited.contains(other)) continue;
                int nd = best + e.getWeight();
                int old = dist.get(other);
                if (nd < old) {
                    if (old != INF) improvements++;
                    dist.put(other, nd);
                    prev.put(other, next);
                } else if (nd == old) {
                    ties = true;
                }
            }
        }
        steps.add(new AlgorithmStep(index, new LinkedHashMap<>(dist), new LinkedHashMap<>(prev),
                new LinkedHashSet<>(visited), null));

        totalDistance = dist.get(endId);
        for (String at = endId; at != null; at = prev.get(at)) shortestPath.add(at);
        Collections.reverse(shortestPath);
    }

    public List<AlgorithmStep> getSteps()   { return steps; }
    public List<String> getShortestPath()   { return shortestPath; }
    public int getTotalDistance()           { return totalDistance; }
    public boolean hasTies()                { return ties; }
    public int getImprovements()            { return improvements; }
}
