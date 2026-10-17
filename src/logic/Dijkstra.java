package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//run dijstra algorithm on graph's current weights once through and records and algorithm st4ep before each pick + stores final shortest path

public class Dijkstra {

    // import model.Graph when added 
    // private final Graph graph;
    private final String startId;     // lecture node
    private final String endId;       // home node

    private final List<AlgorithmStep> steps = new ArrayList<>();
    private List<String> shortestPath = new ArrayList<>();   // ordered node ids, start to end
    private int totalDistance;

    public Dijkstra(/* graph here, */ String startId, String endId) {
        // this.graph = graph;
        this.startId = startId;
        this.endId = endId;
    }

    /** runs algorithm and fills steps + shortestPath. */
    public void run() {
        //MAIN DIJKSTRA LOGIC 
        // set up distances to start = 0, other distances = infinity, previous nodes= null
        //
        // while there are unvisited nodes, loop
        //      set correctNextNode to nearest node with shortest distance
        //      record an AlgorithmStep NOW, using the Set/Map
        //      add node as a visited node
        //      for each node not visited
        //          newDist = distances[node] + edge weight
        //          if newDist < distances[neighbour]
        //              update distance and previous.
        //
        // reverse the path (from endID) to build shortestPath
        //
    }

    public List<AlgorithmStep> getSteps() { return steps; }
    public List<String> getShortestPath() { return shortestPath; }
    public int getTotalDistance() { return totalDistance;}
}