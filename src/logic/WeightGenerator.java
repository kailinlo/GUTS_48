package logic;

import model.Edge;
import model.Graph;
import model.Node;

import java.util.*;

/** Assigns random weights and rejects graphs that would make a boring or ambiguous game. */
public class WeightGenerator {
    public static final int MIN_WEIGHT = 2;
    public static final int MAX_WEIGHT = 15;

    private final Random random;

    public WeightGenerator(long seed) {
        this.random = new Random(seed);
    }

    /** Randomises weights until the graph is good, then returns the solved Dijkstra. */
    public Dijkstra generate(Graph graph, String startId, String endId) {
        int minHops = fewestHops(graph, startId, endId);
        for (int attempt = 0; attempt < 20000; attempt++) {
            for (Edge e : graph.getEdges()) {
                e.setWeight(MIN_WEIGHT + random.nextInt(MAX_WEIGHT - MIN_WEIGHT + 1));
            }
            Dijkstra d = new Dijkstra(graph, startId, endId);
            d.run();
            boolean unique = !d.hasTies();
            boolean teaches = d.getImprovements() >= 1;                     // a known distance gets improved
            boolean notDirect = d.getShortestPath().size() - 1 > minHops;   // shortest route isn't the fewest-hops one
            if (unique && teaches && notDirect) return d;
        }
        throw new IllegalStateException("Could not generate a good graph");
    }

    private int fewestHops(Graph g, String start, String end) {
        Map<String, Integer> hops = new HashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        hops.put(start, 0);
        queue.add(start);
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            for (Node n : g.getNeighbours(cur)) {
                if (!hops.containsKey(n.getId())) {
                    hops.put(n.getId(), hops.get(cur) + 1);
                    queue.add(n.getId());
                }
            }
        }
        return hops.get(end);
    }
}
