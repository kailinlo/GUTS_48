package model;

/** A road between two nodes. Weight = walking minutes, reassigned every game. */
public class Edge {
    private final Node a;
    private final Node b;
    private int weight;

    public Edge(Node a, Node b, int weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    public Node getA()      { return a; }
    public Node getB()      { return b; }
    public int getWeight()  { return weight; }
    public void setWeight(int w) { this.weight = w; }
}
