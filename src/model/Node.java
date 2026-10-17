import java.util.ArrayList;

public class Node {
    public String id;
    public String name;
    public ArrayList<String> position;

    public Node(String id, String name, ArrayList<String> position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getPos() {
        return this.position;
    }
}
