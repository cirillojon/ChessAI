//Sequential Alpha-Beta Implementation
//Very fast

import java.util.Random;
public class SequentialAlphaBeta {

    private static final Random RANDOM = new Random();

    static {
        // Set the seed value for the random number generator
        RANDOM.setSeed(408);
    }

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

    // Method to execute the Alpha-Beta Pruning algorithm sequentially
    private static int alphaBeta(Node node, int depth, int alpha, int beta, boolean maximizingPlayer) {
        // Base case: if the node is a leaf node or depth is 0, return the node's value
        if (node.isLeaf() || depth == 0) {
            return node.value;
        }

        // If maximizing player
        if (maximizingPlayer) {
            int bestValue = Integer.MIN_VALUE;
            for (Node child : node.children) {
                // Compute the value of the child node recursively
                int value = alphaBeta(child, depth - 1, alpha, beta, false);
                // Update the best value seen so far
                bestValue = Math.max(bestValue, value);
                // Update alpha to be the maximum of the current alpha and the best value seen so far
                alpha = Math.max(alpha, bestValue);
                // If beta is less than or equal to alpha, break out of the loop (beta cut-off)
                if (beta <= alpha) {
                    break;
                }
            }
            return bestValue;
        }
        // If minimizing player
        else {
            int bestValue = Integer.MAX_VALUE;
            for (Node child : node.children) {
                // Compute the value of the child node recursively
                int value = alphaBeta(child, depth - 1, alpha, beta, true);
                // Update the best value seen so far
                bestValue = Math.min(bestValue, value);
                // Update beta to be the minimum of the current beta and the best value seen so far
                beta = Math.min(beta, bestValue);
                // If beta is less than or equal to alpha, break out of the loop (alpha cut-off)
                if (beta <= alpha) {
                    break;
                }
            }
            return bestValue;
        }
    }


    public static void main(String[] args) {
        
        int depth = 3; 

        // Creates tree of given depth
        Node root = createLargeTree(depth);

        // STARTS TIME AFTER TREE HAS BEEN CREATED
        long startTime = System.currentTimeMillis();

        // Print the tree
        //root.print("", true);

        // Run Alpha-Beta Pruning on the tree sequentially, with depth 10
        int result = alphaBeta(root, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

        // Print the result
        System.out.println("Result: " + result);

        // Calculate and print the program's running time
        long endTime = System.currentTimeMillis();

        long timeTaken = endTime - startTime;
        System.out.println("Time Taken: " + timeTaken + "ms");
    }
}                    