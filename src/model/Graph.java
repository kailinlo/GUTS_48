import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Graph {
    public Map<String, Node> nodes;
    public Map<String, List<Edge>> adjacentNodes;
    public Map<Edge, Integer> originalWeights;

    public Graph(Map<String, Node> nodes, Map<String, List<Edge>> adjacency) {
        this.nodes = nodes;
        this.adjacentNodes = adjacency;
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacentNodes.put(node.getId(), new ArrayList<>());
    }

    public void addEdge(String idA, String idB, Integer weight) {
        Node a = nodes.get(idA);
        Node b = nodes.get(idB);

        if (a == null || b == null ) {
            throw new IllegalArgumentException("Unknown node in edge: " + idA + " - " + idB);
        }

        Edge edge = new Edge(new ArrayList<>(List.of(a, b)), weight);
        adjacentNodes.get(idA).add(edge);
        adjacentNodes.get(idB).add(edge);

        originalWeights.put(edge, weight);
    }

    public static Graph load(String path) throws IOException {
        String json = Files.readString(Path.of(path));
        JsonObject root = new Gson().fromJson(json, JsonObject.class);

        Graph g = new Graph();

        for (JsonElement el : root.getAsJsonArray("nodes")) {
            JsonObject n = el.getAsJsonObject();
            ArrayList<String> position = new ArrayList<>();

            if (n.has("position")) {
                for (JsonElement p : n.getAsJsonArray("position")) {
                    position.add(p.getAsString());
                }
            }
            
            g.addNode(new Node(
                n.get("id").getAsString(),
                n.has("name") ? n.get("name").getAsString() : n.get("id").getAsString(),
                position));
        }

        for (JsonElement el : root.getAsJsonArray("edges")) {
            JsonObject e = el.getAsJsonObject();
            g.addEdge(
                e.get("from").getAsString(),
                e.get("to").getAsString(),
                e.has("weight") ? e.get("weight").getAsInt() : 1);
        }

        return g;
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Collection<Node> getNodes() {
        return Colletions.unmodifiableCollection(nodes.values());
    }

    public List<Edge> getEdges(String nodeId) {
        return Collections.unmodifiableList(adjacentNodes.getOrDefault(nodeId, List.of()));
    }

    public Node getOtherNode(Edge edge, String nodeId) {
        for (Node n : edge.getConnectedNodes()) {
            if (!n.getId().equals(nodeID)) return n;
        }

        return null;
    }

    public List<Node> getNeighbours(String nodeId) {
        List<Node> result = new ArrayList<>();
        for (Edge e : getEdges(nodeId)) {
            result.add(getOtherNode(e, nodeId));
        }

        return result;
    }

    public void setAllWeights(Integer weight) {
        for (Edge e : originalWeights.keySet()) {
            e.setWeight(weight);
        }
    }

    public void resetWeights() {
        for (Map.Entry<Edge, Integer> entry : originalWeights.entrySet()) {
            entry.getKey().setWeight(entry.getValue());
        }
    }
}