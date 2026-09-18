package com.edc.binaryencrypt.main.trees;

/**
 * Binary Search Tree (BST) for storing enriched nodes containing:
 * - caractere: the original character
 * - valorAscii: the ASCII value used for BST ordering (left <=, right >)
 * - posicaoOriginal: the original index of the character in the input string
 */
public class BinaryTree {
    private Node root;

    public BinaryTree() {
        this.root = null;
    }

    /**
     * Constructs a BST by inserting each character of the given string as a node.
     * Each node stores the character, its ASCII value, and its original position.
     * @param text The string to insert into the BST.
     */
    public BinaryTree(String text) {
        this();
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                insert(c, (int) c, i);
            }
        }
    }

    /**
     * Inserts a node with the given character, ASCII value, and original position.
     * Duplicates (equal ASCII values) are inserted to the left.
     * @param caractere The character.
     * @param valorAscii The ASCII value used for ordering.
     * @param posicaoOriginal The original position in the input string.
     */
    public void insert(char caractere, int valorAscii, int posicaoOriginal) {
        root = insertRec(root, caractere, valorAscii, posicaoOriginal);
    }

    private Node insertRec(Node root, char caractere, int valorAscii, int posicaoOriginal) {
        if (root == null) {
            return new Node(caractere, valorAscii, posicaoOriginal);
        }
        if (valorAscii <= root.valorAscii) {
            root.left = insertRec(root.left, caractere, valorAscii, posicaoOriginal);
        } else {
            root.right = insertRec(root.right, caractere, valorAscii, posicaoOriginal);
        }
        return root;
    }

    /**
     * Serializes the tree in pre-order traversal, including null markers.
     * Each node is represented as "valorAscii:caractere:posicaoOriginal".
     * Null markers are represented as '#'.
     * Fields are separated by ':', nodes by ','.
     * @return A string representing the pre-order serialization.
     */
    public String serializePreOrder() {
        StringBuilder sb = new StringBuilder();
        serializePreOrderRec(root, sb);
        return sb.toString();
    }

    private void serializePreOrderRec(Node node, StringBuilder sb) {
        if (node == null) {
            sb.append('#').append(',');
            return;
        }
        sb.append(node.valorAscii).append(':')
          .append(node.caractere).append(':')
          .append(node.posicaoOriginal).append(',');
        serializePreOrderRec(node.left, sb);
        serializePreOrderRec(node.right, sb);
    }

    /**
     * Deserializes the tree from a pre-order string with null markers.
     * Expected format: "valorAscii:caractere:posicaoOriginal,value,...,#"
     * @param data The serialized string.
     */
    public void deserializePreOrder(String data) {
        if (data == null || data.isEmpty()) {
            root = null;
            return;
        }
        String[] tokens = data.split(",");
        int[] index = {0};
        root = deserializePreOrderRec(tokens, index);
    }

    private Node deserializePreOrderRec(String[] tokens, int[] index) {
        if (index[0] >= tokens.length) {
            return null;
        }
        String token = tokens[index[0]];
        index[0]++;

        if (token.equals("#")) {
            return null;
        }

        // Expected format: valorAscii:caractere:posicaoOriginal
        String[] parts = token.split(":");
        if (parts.length != 3) {
            // Malformed token, treat as null to avoid crash
            return null;
        }
        int valorAscii = Integer.parseInt(parts[0]);
        char caractere = parts[1].charAt(0); // assuming single char
        int posicaoOriginal = Integer.parseInt(parts[2]);

        Node node = new Node(caractere, valorAscii, posicaoOriginal);
        node.left = deserializePreOrderRec(tokens, index);
        node.right = deserializePreOrderRec(tokens, index);
        return node;
    }

    /**
     * Recovers the original message from the BST by using the posicaoOriginal field.
     * @return The original message string.
     */
    public String recoverOriginalMessage() {
        if (root == null) {
            return "";
        }
        // First, find the maximum position to know the size
        int maxPos = findMaxPosition(root);
        char[] chars = new char[maxPos + 1];
        fillCharsFromTree(root, chars);
        return new String(chars);
    }

    private int findMaxPosition(Node node) {
        if (node == null) {
            return -1;
        }
        int max = node.posicaoOriginal;
        int leftMax = findMaxPosition(node.left);
        int rightMax = findMaxPosition(node.right);
        return Math.max(max, Math.max(leftMax, rightMax));
    }

    private void fillCharsFromTree(Node node, char[] chars) {
        if (node == null) {
            return;
        }
        chars[node.posicaoOriginal] = node.caractere;
        fillCharsFromTree(node.left, chars);
        fillCharsFromTree(node.right, chars);
    }

    /**
     * Returns the root node for visualization purposes.
     * @return The root node
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Checks if the tree is empty.
     * @return true if the tree has no nodes, false otherwise.
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Inner class representing a node in the BST with enriched metadata.
     */
    public static class Node {
        public char caractere;
        public int valorAscii;
        public int posicaoOriginal;
        public Node left;
        public Node right;

        public Node(char caractere, int valorAscii, int posicaoOriginal) {
            this.caractere = caractere;
            this.valorAscii = valorAscii;
            this.posicaoOriginal = posicaoOriginal;
            this.left = null;
            this.right = null;
        }
    }
}