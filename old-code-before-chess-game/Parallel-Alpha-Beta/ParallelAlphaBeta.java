//Current Parallel Alpha-Beta-Pruning implementation. 
//Still much slower than the sequential implementation, but it is a start.

import java.util.concurrent.*;
import java.util.Random;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ParallelAlphaBeta {

    // Get the number of available processors
    public static int MAX_THREADS = Runtime.getRuntime().availableProcessors();

/*  The following line creates a ThreadLocal cache that holds a separate HashMap instance for each thread that accesses it. 
    The withInitial() method of the ThreadLocal class creates a new HashMap instance when a thread first accesses the cache variable.
    Each thred having their own cache reduces contention and also prevents raceconditions that could happen 
    when accessing and updating the cache from multiple threads simultaneously.  */

    public static ThreadLocal<HashMap<Node, Integer>> cache = ThreadLocal.withInitial(HashMap::new);

    //Random variable used to generate a given seed for testing purposes
    public static Random RANDOM = new Random(408);

    // Method to create a tree with any given depth and fill the leaves with random values
    public static Node createLargeTree(int depth) {
        if (depth == 0) {
            return new Node(RANDOM.nextInt(100), null);
        } else {
            Node leftChild = createLargeTree(depth - 1);
            Node rightChild = createLargeTree(depth - 1);
            Node[] children = new Node[] {leftChild, rightChild};
            return new Node(0, children);
        }
    }

    //Node class used to implement the tree
    public static class Node {
        int value;
        Node[] children;

        public Node(int value, Node[] children) {
            this.value = value;
            this.children = children;
        }

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

    //A RecursiveTask class that implements the alpha-beta algorithm for finding the optimal solution of a two-player game.
    //The task is divided recursively into subtasks to evaluate subsets of the game tree.

/*  RecursiveTask is a Java Concurrency API abstract subclass of ForkJoinTask that is used for parallel computations. 
    It is suitable for computations that can be divided into smaller, independent subtasks that can be executed in parallel. 
    When a RecursiveTask is submitted to a ForkJoinPool, it is divided into smaller subtasks until they can be solved directly. 
    The subtasks' results are then combined to produce the final result of the task. 
    The AlphaBetaTask class uses RecursiveTask to parallelize the computation of the optimal solution in the alpha-beta algorithm for two-player games. */

    public static class AlphaBetaTask extends RecursiveTask<Node> {
        // The node to evaluate
        private Node node;

        // The maximum depth of the game tree to search
        private int depth;

        // The current alpha value (the best value found so far for the maximizing player)
        private int alpha;

        // The current beta value (the best value found so far for the minimizing player)
        private int beta;

        // A boolean indicating whether the current player is the maximizing player
        private boolean maximizingPlayer;

        /**
         * Constructor for creating an AlphaBetaTask object.
         *
         * @param node - the node to evaluate
         * @param depth - the maximum depth of the game tree to search
         * @param alpha - the current alpha value (the best value found so far for the maximizing player)
         * @param beta - the current beta value (the best value found so far for the minimizing player)
         * @param maximizingPlayer - a boolean indicating whether the current player is the maximizing player
         */
        public AlphaBetaTask(Node node, int depth, int alpha, int beta, boolean maximizingPlayer) {
            this.node = node;
            this.depth = depth;
            this.alpha = alpha;
            this.beta = beta;
            this.maximizingPlayer = maximizingPlayer;
        }

      /* Computes the best node for the given node using alpha-beta pruning and memoization.
         It first retrieves the local cache for this task, and checks if the given node has a cached value.
         If a cached value exists, it returns the node. If the node is a leaf node or the depth limit has been reached,
         it returns the node. If the node is a maximizing player, it computes the best node by invoking computeMax() method.
         Otherwise, it computes the best node by invoking computeMin() method.
         Returns the best node for the given node */

        public Node compute() {
            // Retrieve the local cache for this task
            HashMap<Node, Integer> localCache = cache.get();

            // Check if the given node has a cached value, return the node if a cached value exists
            Integer cachedValue = localCache.get(node);
            if (cachedValue != null) {
                return node;
            }

            // If the node is a leaf node or the depth limit has been reached, return the node
            if (node.isLeaf() || depth == 0) {
                return node;
            }

            // If the node is a maximizing player, compute the best node using computeMax() method
            if (maximizingPlayer) {
                return computeMax();
            }
            // Otherwise, compute the best node using computeMin() method
            else {
                return computeMin();
            }
        }

        // The technique of creating a priority queue to order the search based on a heuristic evaluation function is called Best First Search. 
        // In this case, the heuristic is the cached value of each child node. 
        // The PriorityQueue orders the children based on their cached values, so that the node with the best cached value is evaluated first. 
        // This can lead to more efficient search by exploring the most promising branches first.


        // Method to compute the best value and node for a minimizing player
        private Node computeMax() {
            // Retrieve the local cache for this task from the thread-local cache
            HashMap<Node, Integer> localCache = cache.get();

            // The follow lines creates a priority queue to order the children of the current node based on their cached values
            // The Comparator passed to the PriorityQueue constructor compares 
            // the cached values of each child node in descending order
            // If a child node has not been evaluated before, its cached value defaults to 0

            PriorityQueue<Node> orderedChildren = new PriorityQueue<>(node.children.length,Comparator.comparingInt(n -> -localCache.getOrDefault(n, 0)));

            // Add the children of the current node to the priority queue
            orderedChildren.addAll(Arrays.asList(node.children));
            Node bestNode = null;
            int bestValue = Integer.MIN_VALUE;
            // Evaluate each child node recursively
            for (Node child : orderedChildren) {
                // Create a new task for the child node
                AlphaBetaTask task = new AlphaBetaTask(child, depth - 1, alpha, beta, false);
                // Print the name of the current thread before forking the task
                System.out.println("Thread " + Thread.currentThread().getName() + " forking task for child " + child.value + " in max");
                // Fork the task to execute it on a separate thread
                task.fork();
                // Print the name of the current thread after joining the task
                System.out.println("Thread " + Thread.currentThread().getName() + " joining task for child " + child.value + " in max");
                // Join the result of the task to retrieve the value and node
                Node result = task.join();
                int value = result.value;
                // Update the best node and value if the current value is better
                if (value > bestValue) {
                    bestValue = value;
                    bestNode = result;
                }
                // Update the alpha value to the maximum of the current alpha value and best value
                alpha = Math.max(alpha, bestValue);
                // Perform alpha-beta pruning if beta <= alpha
                if (alpha >= beta) {
                    System.out.println("Pruned node: " + child.value);
                    break;
                }
            }

            // Cache the best node and value for future use
            localCache.put(bestNode, bestValue);
            // Return the best node
            return bestNode;
        }

        // Method to compute the best value and node for a minimizing player
        private Node computeMin() {
            // Retrieve the local cache for this task
            HashMap<Node, Integer> localCache = cache.get();

            // The following line creates a priority queue to order the children of the current node based on their cached values (ascending order)
            // The Comparator passed to the PriorityQueue constructor compares the cached values of each child node in ascending order
            // If a child node has not been evaluated before, its cached value defaults to 0

            PriorityQueue<Node> orderedChildren = new PriorityQueue<>(node.children.length, Comparator.comparingInt(n -> localCache.getOrDefault(n, 0)));
            orderedChildren.addAll(Arrays.asList(node.children));

            // Initialize variables to track the best value and node
            Node bestNode = null;
            int bestValue = Integer.MAX_VALUE;

            // Iterate through the ordered children of the node
            for (Node child : orderedChildren) {
                // Create a new task to search the child node
                AlphaBetaTask task = new AlphaBetaTask(child, depth - 1, alpha, beta, true);

                // Print the name of the current thread before forking the task
                System.out.println("Thread " + Thread.currentThread().getName() + " forking task for child " + child.value + " in min");

                // Fork the task to execute it on a separate thread
                task.fork();

                // Print the name of the current thread after joining the task
                System.out.println("Thread " + Thread.currentThread().getName() + " joining task for child " + child.value + " in min");

                // Join the result of the child task
                Node result = task.join();

                // Retrieve the value of the child node from the result
                int value = result.value;

                // Update the best value and node if the child's value is better
                if (value < bestValue) {
                    bestValue = value;
                    bestNode = result;
                }

                // Update beta with the minimum of beta and the best value
                beta = Math.min(beta, bestValue);

                // If beta is less than or equal to alpha, prune the remaining children and stop iterating
                if (beta <= alpha) {
                    System.out.println("Pruned node: " + child.value);
                    break;
                }

            }

            // Cache the best value and node for this node
            localCache.put(bestNode, bestValue);

            // Return the best node for the current node
            return bestNode;
        }
    }

            // Method to print results of the algorithm
            public static void printResults(Node root, Node result, int depth, long timeTaken) {
                System.out.println("Parallel Alpha-Beta Algorithm Results:");
                System.out.println("=======================================");
                System.out.println("Depth: " + depth);
                System.out.println("Number of Threads: " + MAX_THREADS);
                System.out.println("Time Taken: " + timeTaken + "ms");
                System.out.println("Result:");
                System.out.println("-------");
                System.out.println("Max Value: " + result.value);
            }
            
            // Driver Method
            public static void main(String[] args) {
                int depth = 3;
    
                // Creates tree for a given depth
                Node root = createLargeTree(depth);
    
                //STARTS TIME AFTER TREE HAS BEEN CREATED
                long startTime = System.currentTimeMillis();
    
                // Prints out the tree
                root.print("", true);
    
                int alpha = Integer.MIN_VALUE;
                int beta = Integer.MAX_VALUE;
    
                // Create a new AlphaBetaTask with the root node, maximum depth, alpha and beta values, and set to the maximizing player
                AlphaBetaTask rootTask = new AlphaBetaTask(root, depth, alpha, beta, true);
    
                // Create a new ForkJoinPool with the maximum number of threads available on the system
                ForkJoinPool pool = new ForkJoinPool(MAX_THREADS);
    
                // Invoke the root task using the ForkJoinPool and get the result
                Node result = pool.invoke(rootTask);
    
                // Shut down the ForkJoinPool after the task is complete
                pool.shutdown();
    
                long endTime = System.currentTimeMillis();
                long timeTaken = endTime - startTime;
    
                // Print the results
                printResults(root, result, depth, timeTaken);
            }
}