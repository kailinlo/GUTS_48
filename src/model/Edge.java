public class Edge {
    public ArrayList<Node> connectedNodes;
    public Integer weight;

    public Edge(ArrayList<Node> connectedNodes, Integer weight) {
        self.connectedNodes = connectedNodes;
        self.weight = weight;
    }

    public void setWeight(Integer newWeight) {
        self.weight = newWeight;
    }

    public ArrayList<Node> getConnectedNodes() {
        return self.connectedNodes;
    }
}