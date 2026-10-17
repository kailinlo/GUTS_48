import java.util.ArrayList;

public class Edge {
    public ArrayList<Node> connectedNodes;
    public Integer weight;

    public Edge(ArrayList<Node> nodes, Integer weight) {
        this.connectedNodes = nodes;
        this.weight = weight;
    }

    public void setWeight(Integer newWeight) {
        this.weight = newWeight;
    }

    public ArrayList<Node> getConnectedNodes() {
        return this.connectedNodes;
    }
}