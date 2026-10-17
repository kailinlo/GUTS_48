package model;

import java.util.*;

/** The six-node Glasgow map. Layout is fixed; weights are filled in by WeightGenerator. */
public class Graph {
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacency.put(node.getId(), new ArrayList<>());
    }

    public void addEdge(String idA, String idB, int weight) {
        Node a = nodes.get(idA);
        Node b = nodes.get(idB);
        if (a == null || b == null) {
            throw new IllegalArgumentException("Unknown node in edge: " + idA + " - " + idB);
        }
        Edge e = new Edge(a, b, weight);
        edges.add(e);
        adjacency.get(idA).add(e);
        adjacency.get(idB).add(e);
    }

    /** Matches the wireframe. Weights are placeholders (1) until randomised. */
    public static Graph createDefault() {
        Graph g = new Graph();
        g.addNode(new Node("lecture", "Lecture",   110, 270));
        g.addNode(new Node("boyd",    "Boyd Orr",  270, 140));
        g.addNode(new Node("ovo",     "OVO Hydro", 540, 140));
        g.addNode(new Node("clyde",   "The Clyde", 270, 390));
        g.addNode(new Node("wunder",  "Wunderbar", 540, 390));
        g.addNode(new Node("home",    "Home",      700, 270));

        g.addEdge("lecture", "boyd", 1);
        g.addEdge("lecture", "clyde", 1);
        g.addEdge("boyd", "clyde", 1);
        g.addEdge("boyd", "ovo", 1);
        g.addEdge("clyde", "ovo", 1);
        g.addEdge("clyde", "wunder", 1);
        g.addEdge("ovo", "wunder", 1);
        g.addEdge("ovo", "home", 1);
        g.addEdge("wunder", "home", 1);
        return g;
    }

    public Node getNode(String id) { return nodes.get(id); }

    public Collection<Node> getNodes() { return Collections.unmodifiableCollection(nodes.values()); }

    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }

    public List<Edge> getEdges(String nodeId) {
        return Collections.unmodifiableList(adjacency.getOrDefault(nodeId, List.of()));
    }

    public Node getOtherNode(Edge edge, String nodeId) {
        return edge.getA().getId().equals(nodeId) ? edge.getB() : edge.getA();
    }

    public List<Node> getNeighbours(String nodeId) {
        List<Node> result = new ArrayList<>();
        for (Edge e : getEdges(nodeId)) result.add(getOtherNode(e, nodeId));
        return result;
    }

    /** The road between two nodes, or null if there isn't one. */
    public Edge getEdge(String idA, String idB) {
        for (Edge e : getEdges(idA)) {
            if (getOtherNode(e, idA).getId().equals(idB)) return e;
        }
        return null;
    }
}
