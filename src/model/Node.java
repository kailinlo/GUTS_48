package model;

/** One place on the map. x/y are positions in an 800x500 "map space" (MapPanel scales them). */
public class Node {
    private final String id;
    private final String name;
    private final int x;
    private final int y;

    public Node(String id, String name, int x, int y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getId()   { return id; }
    public String getName() { return name; }
    public int getX()       { return x; }
    public int getY()       { return y; }

    @Override public boolean equals(Object o) {
        return o instanceof Node && ((Node) o).id.equals(id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return name; }
}
