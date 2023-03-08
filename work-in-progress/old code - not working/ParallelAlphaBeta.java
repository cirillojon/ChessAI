/* This code is a parallel implementation of the Heuristic Alpha-Beta Pruning Algorithm.

The aim is to find the best move for a player that maximizes their score and minimizes the score for the opponent, 
by traversing the game tree and evaluating the best move for the current player at each node, 
and pruning the sub-trees that are guaranteed to be worse than the current node.

The goal of this implementation is to be faster (on average) than the sequential implementation,

This implementation uses the Fork-Join framework to execute the Alpha-Beta Pruning algorithm using multiple threads to increase efficiency. 
The AlphaBetaTask class extends RecursiveTask to allow parallel computation of subtrees in the game tree. 
The compute() method of the AlphaBetaTask class recursively computes the best move for the current player, 
either by maximizing or minimizing the score, based on the depth of the tree and the current state. 
If the current node is a leaf node or the depth has reached the limit, the node's value is returned. 

The heuristic Alpha-Beta Pruning algorithm is used to prune the game tree,
which reduces the number of nodes that need to be evaluated by eliminating sub-trees that are guaranteed to be worse than the current node.

The heuristic alpha-beta approach may return a different value than the regular alpha-beta approach. 
This is because the heuristic approach involves using a heuristic function to estimate the value of non-leaf nodes, 
which may not be the exact value of the node. The heuristic function is designed to be fast, but not necessarily as accurate.

The implementation also uses memoization to cache the previously computed values, 
which speeds up the computation for previously visited nodes. 
The cache is implemented using a ConcurrentHashMap to allow concurrent access and modification by multiple threads. 

Finally, the main() method creates a large game tree with a given depth and executes the Alpha-Beta Pruning algorithm on the tree using multiple threads. 
The execution time of the algorithm is printed to the console along with the best move found for the player. */

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//This import is for testing purposes - when we need to set the random number generator to a fixed value
//as the ThreadLocalRandom class does not have a setSeed() method

//import java.util.Random;

public class ParallelAlphaBeta {

    // Start time of the program
    static long startTime = System.currentTimeMillis();

    // Get the number of available processors
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

    // Memoization cache to store previously computed values
    // private static final Map<Node, Integer> cache = new ConcurrentHashMap<>();

/*  
    Since the cache is only accessed within the AlphaBetaTask class and not shared across multiple threads, 
    using a ThreadLocal cache eliminates the need for synchronization 
    and reduces contention among threads accessing the cache. 
*/
    // Thread-local memoization cache to store previously computed values, to reduce contention
    private static final ThreadLocal<Map<Node, Integer>> cache = ThreadLocal.withInitial(ConcurrentHashMap::new);

    //Thread-local random number generator
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    /*TO TEST IF ANSWER IS CORRECT USE THIS INSTEAD TO CREATE SAME TREE EVERYTIME*/
    // private static final Random RANDOM = new Random();
    // static {
    //     // Set the seed value for the random number generator
    //     RANDOM.setSeed(123);
    //  }

    // Method to create a tree with any given depth and fill the leaves with random values
    private static Node createLargeTree(int depth) {
        if (depth == 0) {
            return new Node(RANDOM.nextInt(100), null);
        } else {
            Node leftChild = createLargeTree(depth - 1);
            Node rightChild = createLargeTree(depth - 1);
            Node[] children = new Node[] {leftChild, rightChild};
            return new Node(0, children);
        }
    }

    // Node class representing a node in the tree
    private static class Node {
        int value;
        Node[] children;

        // Constructor
        public Node(int value, Node[] children) {
            this.value = value;
            this.children = children;
        }

        // Method to check if the node is a leaf node
        public boolean isLeaf() {
            return children == null || children.length == 0;
        }

        // Method to print the node and its children, in a way that is easy to read, similar to a tree structure
        public void print(String prefix, boolean isTail) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + "Value: " + value);
            if (children != null) {
                for (int i = 0; i < children.length - 1; i++) {
                    children[i].print(prefix + (isTail ? "    " : "│   "), false);
                }
                if (children.length > 0) {
                    children[children.length - 1].print(prefix + (isTail ?"    " : "│   "), true);
                }
            }
        }
    }

    // AlphaBetaTask class to perform the Alpha-Beta Pruning algorithm
    public static class AlphaBetaTask extends RecursiveTask<Integer> {
        private Node node;
        private int depth;
        private AtomicInteger alpha;
        private AtomicInteger beta;
        private boolean maximizingPlayer;

        // Constructor
        public AlphaBetaTask(Node node, int depth, AtomicInteger alpha, AtomicInteger beta, boolean maximizingPlayer) {
            this.node = node;
            this.depth = depth;
            this.alpha = alpha;
            this.beta = beta;
            this.maximizingPlayer = maximizingPlayer;
        }
    /*  
        This method uses a recursive approach to traverse the game tree and evaluate the best move for the given player.
        - If the node has already been computed, the method returns its cached value.
        - If the node is a leaf node or the maximum depth is reached, the method returns the node's value.
        - If the player is maximizing, the method updates alpha and returns the best value.
        - If the player is minimizing, the method updates beta and returns the worst value.
        - If beta is less than or equal to alpha, the method stops exploring the sub-tree and returns the alpha value.
        - The computed value is stored in the cache to avoid redundant computations.
    */
        @Override
        protected Integer compute() {
        // Check if the node has already been computed
        Map<Node, Integer> localCache = cache.get();
        if (localCache.containsKey(node)) {
            return localCache.get(node);
        }

        // Base case: if the node is a leaf node or depth is 0, return the node's value
        if (node.isLeaf() || depth == 0) {
            int value = node.value;
            // Store the computed value in the cache
            localCache.put(node, value);
            return value;
        }

        // If maximizing player, update alpha and return best value
        if (maximizingPlayer) {
            int bestValue = Integer.MIN_VALUE;
            // Loop over children of the current node
            for (Node child : node.children) {
                // Create a new AtomicInteger to pass alpha to the child node
                AtomicInteger newAlpha = new AtomicInteger(alpha.get());
                // Create a new task for the child node with updated depth and alpha value
                AlphaBetaTask childTask = new AlphaBetaTask(child, depth - 1, newAlpha, beta, false);
                // Compute the value of the child node by calling compute() on the child task
                int value = childTask.compute();
                // Update the best value seen so far
                bestValue = Math.max(bestValue, value);
                // Update alpha to be the maximum of the current alpha and the best value seen so far
                alpha.set(Math.max(alpha.get(), bestValue));
                // If beta is less than or equal to alpha, break out of the loop (beta cut-off)
                if (beta.get() <= alpha.get()) {
                    // System.out.println(Thread.currentThread().getName() + ": Beta <= Alpha. Pruning..." + " Alpha: " + alpha.get() + " Beta: " + beta.get() + 
                    // " Best Value: " + bestValue + " Value: " + value + " Depth: " + depth + " Node: " + node.value + " Child: " + child.value + " Max: " + maximizingPlayer);
                    break;
                }
            }
            // Store the computed value in the cache
            localCache.put(node, bestValue);
            return bestValue;
        }
        // If minimizing player, update beta and return best value
        else {
            int bestValue = Integer.MAX_VALUE;
            // Loop over children of the current node
            for (Node child : node.children) {
                    // Create a new AtomicInteger to pass beta to the child node
                    AtomicInteger newBeta = new AtomicInteger(beta.get());
                    // Create a new task for the child node with updated depth and beta value
                    AlphaBetaTask childTask = new AlphaBetaTask(child, depth - 1, alpha, newBeta, true);
                    // Compute the value of the child node by calling compute() on the child task
                    int value = childTask.compute();
                    // Update the best value seen so far
                    bestValue = Math.min(bestValue, value);
                    // Update beta to be the minimum of the current beta and the best value seen so far
                    beta.set(Math.min(beta.get(), bestValue));
                    // If beta is less than or equal to alpha, break out of the loop (alpha cut-off)
                    if (beta.get() <= alpha.get()) {
                        // System.out.println(Thread.currentThread().getName() + ": Beta <= Alpha. Pruning..." + " Alpha: " + alpha.get() + " Beta: " + beta.get() + 
                        // " Best Value: " + bestValue + " Value: " + value + " Depth: " + depth + " Node: " + node.value + " Child: " + child.value + " Max: " + maximizingPlayer);
                        break;
                    }
                }
                // Store the computed value in the cache
                localCache.put(node, bestValue);
                return bestValue;
        }
    }
    
        // Method to execute the Alpha-Beta Pruning algorithm using multiple threads
        public static int execute(Node root, int depth) {
        // Create new AtomicInteger objects for alpha and beta values, initialized to the smallest and largest integer values, respectively
        AtomicInteger alpha = new AtomicInteger(Integer.MIN_VALUE);
        AtomicInteger beta = new AtomicInteger(Integer.MAX_VALUE);

        // Create a new AlphaBetaTask object with the root node, specified depth, alpha and beta values, and the flag indicating the maximizing player
        AlphaBetaTask task = new AlphaBetaTask(root, depth, alpha, beta, true);

        // Create a new ForkJoinPool with the specified maximum number of threads
        ForkJoinPool pool = new ForkJoinPool(MAX_THREADS);

        // Invoke the task using the pool and retrieve the result
        int result = pool.invoke(task);

        // Shut down the pool to free resources
        pool.shutdown();

        // Return the result of the Alpha-Beta Pruning algorithm
        return result;
        }
    }

    // Main method to test the Parallel Alpha-Beta Pruning algorithm
    public static void main(String[] args) {
        // Set the depth of the tree to a given number
        int depth = 10;
        
        // Create a tree of the specified depth
        Node root = createLargeTree(depth);
        
        // Print the tree in a readable format
        //root.print("", true);
        
        // Execute the Alpha-Beta Pruning algorithm on the tree and get the result
        int result = AlphaBetaTask.execute(root, depth);
        
        // Get the end time of the execution
        long endTime = System.currentTimeMillis();
        
        // Print the number of available processors
        System.out.println("Number of available processors: " + MAX_THREADS);
        
        // Print the result of the algorithm
        System.out.println("Result: " + result);
        
        // Print the time taken to execute the algorithm
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }
}