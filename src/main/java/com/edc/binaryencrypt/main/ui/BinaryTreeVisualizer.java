package com.edc.binaryencrypt.main.ui;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import com.edc.binaryencrypt.main.trees.BinaryTree.Node;

import java.awt.*;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Widget to visualize a binary tree.
 * Draws nodes as circles with values and connects them with lines.
 */
public class BinaryTreeVisualizer extends JPanel {
    private BinaryTree tree;
    private static final int NODE_RADIUS = 20;
    private static final int VERTICAL_SPACING = 60;
    private static final int HORIZONTAL_SPACING = 100;

    public BinaryTreeVisualizer() {
        this.tree = new BinaryTree(); // Empty tree
        setPreferredSize(new Dimension(400, 300));
        setBackground(new Color(43, 45, 66)); // #2B2D42
    }

    /**
     * Sets the binary tree to visualize.
     * @param tree The binary tree to display
     */
    public void setBinaryTree(BinaryTree tree) {
        this.tree = tree;
        repaint(); // Trigger repaint
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (tree == null) {
            return;
        }

        if (tree.isEmpty()) {
            // Draw placeholder text
            g2d.setColor(new Color(138, 149, 183)); // #8A95B7
            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth("Árvore vazia");
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2 + fm.getAscent() / 2;
            g2d.drawString("Árvore vazia", x, y);
            return;
        }

        // Calculate positions for nodes
        Map<Node, Point> nodePositions = new HashMap<>();
        calculateNodePositions(tree.getRoot(), 0, 0, getWidth() / 2, 50, nodePositions);

        // Draw lines first (so they appear under nodes)
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(138, 149, 183)); // #8A95B7
        drawLines(g2d, tree.getRoot(), nodePositions);

        // Draw nodes
        drawNodes(g2d, tree.getRoot(), nodePositions);
    }

    /**
     * Calculates positions for all nodes in the tree.
     */
    private void calculateNodePositions(Node node, int depth, int index, int xCenter, int xOffset, Map<Node, Point> positions) {
        if (node == null) {
            return;
        }

        // Calculate x position based on index and depth
        int x = xCenter - (int)(xOffset * (Math.pow(2, depth) - 1 - 2 * index));
        int y = 50 + depth * VERTICAL_SPACING;

        positions.put(node, new Point(x, y));

        // Recursively calculate positions for children
        calculateNodePositions(node.left, depth + 1, index * 2, xCenter, xOffset / 2, positions);
        calculateNodePositions(node.right, depth + 1, index * 2 + 1, xCenter, xOffset / 2, positions);
    }

    /**
     * Draws lines connecting nodes.
     */
    private void drawLines(Graphics2D g2d, Node node, Map<Node, Point> positions) {
        if (node == null) {
            return;
        }

        Point pos = positions.get(node);
        if (pos == null) {
            return;
        }

        if (node.left != null) {
            Point leftPos = positions.get(node.left);
            if (leftPos != null) {
                g2d.drawLine(pos.x, pos.y, leftPos.x, leftPos.y);
                drawLines(g2d, node.left, positions);
            }
        }

        if (node.right != null) {
            Point rightPos = positions.get(node.right);
            if (rightPos != null) {
                g2d.drawLine(pos.x, pos.y, rightPos.x, rightPos.y);
                drawLines(g2d, node.right, positions);
            }
        }
    }

    /**
     * Draws nodes as circles with values.
     */
    private void drawNodes(Graphics2D g2d, Node node, Map<Node, Point> positions) {
        if (node == null) {
            return;
        }

        Point pos = positions.get(node);
        if (pos == null) {
            return;
        }

        // Draw circle
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS,
                NODE_RADIUS * 2, NODE_RADIUS * 2));
        g2d.setColor(new Color(58, 63, 88)); // #3A3F58
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS,
                NODE_RADIUS * 2, NODE_RADIUS * 2));

        // Draw value (the ASCII value)
        g2d.setColor(new Color(51, 51, 51)); // #333333
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String valueText = String.valueOf(node.valorAscii);
        int textWidth = fm.stringWidth(valueText);
        int textHeight = fm.getAscent();
        int x = pos.x - textWidth / 2;
        int y = pos.y + textHeight / 2 - fm.getDescent();
        g2d.drawString(valueText, x, y);

        // Recursively draw children
        drawNodes(g2d, node.left, positions);
        drawNodes(g2d, node.right, positions);
    }
}