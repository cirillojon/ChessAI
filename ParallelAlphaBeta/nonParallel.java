    import java.util.Random;

    //non-parallel implementation for testing
    public class nonParallel{
    
        static long startTime = System.currentTimeMillis();

        private static final Random RANDOM = new Random();

        
    static {
        // Set the seed value for the random number generator
        RANDOM.setSeed(123);
     }
        
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
        
        private static class Node {
            int value;
            Node[] children;
        
            public Node(int value, Node[] children) {
                this.value = value;
                this.children = children;
            }
        
            public boolean isLeaf() {
                return children == null || children.length == 0;
            }
        
            public void print(String prefix, boolean isTail) {
                System.out.println(prefix + (isTail ? "└── " : "├── ") + "Value: " + value);
                if (children != null) {
                    for (int i = 0; i < children.length - 1; i++) {
                        children[i].print(prefix + (isTail ? "    " : "│   "), false);
                    }
                    if (children.length > 0) {
                        children[children.length - 1].print(prefix + (isTail ? "    " : "│   "), true);
                    }
                }
            }
        }
        
        public static int alphaBeta(Node node, int depth, int alpha, int beta, boolean maximizingPlayer) {
            if (node.isLeaf() || depth == 0) {
                return node.value;
            }
        
            if (maximizingPlayer) {
                int value = Integer.MIN_VALUE;
                for (Node child : node.children) {
                    int childValue = alphaBeta(child, depth - 1, alpha, beta, false);
                    value = Math.max(value, childValue);
                    alpha = Math.max(alpha, value);
                    if (alpha >= beta) {
                        break;
                    }
                }
                return value;
            } else {
                int value = Integer.MAX_VALUE;
                for (Node child : node.children) {
                    int childValue = alphaBeta(child, depth - 1, alpha, beta, true);
                    value = Math.min(value, childValue);
                    beta = Math.min(beta, value);
                    if (alpha >= beta) {
                        break;
                    }
                }
                return value;
            }
        }
    
        public static void main(String[] args) {
            int depth = 10;
            Node root = createLargeTree(depth);
            //root.print("", true);
        
            int result = alphaBeta(root, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            long endTime = System.currentTimeMillis();
        
            System.out.println("Result: " + result);
            System.out.println("Runtime: " + (endTime - startTime) + "ms");
        }        
    }
    
    
    
            