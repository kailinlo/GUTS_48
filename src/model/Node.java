import java.util.ArrayList;

public class Node {
    public String id;
    public String name;
    public ArrayList<String> position;

    public Node(String id, String name, ArrayList<String> position) {
        self.id = id;
        self.name = name;
        self.position = position;
    }

    public String getId() {
        return self.id;
    }

    public String getName() {
        return self.name;
    }

    public ArrayList<String> getPos() {
        return self.position;
    }
}
