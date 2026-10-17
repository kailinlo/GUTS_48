package logic;

import java.util.Random;
//assigning random edge weights + rejecting graphs that don't make game interesting 
//option to reload demo edge weights (seed for this is logged)


public class WeightGenerator {

    private static final int MIN_WEIGHT = 2;       // minutes
    private static final int MAX_WEIGHT = 15;
    private static final int MAX_ATTEMPTS = 1000;  // loop max so it doesn't go on forever 

    private final long seed;
    private final Random random;

    //random seed generator 
    public WeightGenerator() {
        this(System.currentTimeMillis());
    }

    //demo seed generator + logging 
    public WeightGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        System.out.println("WeightGenerator seed: " + seed);
    }

    // assigning weights to all edges
    public void generate(/*graph here*/) {
        //logic here 
    }

    private boolean isInteresting(/*graph, dijkstra result*/) {
        //logic here 
        return true;
    }

    public long getSeed() { return seed; }
}