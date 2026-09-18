package com.edc.binaryencrypt.main.ui;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Visualizador de Árvore Binária de Busca (BST) para exibir os nós com seus valores ASCII.
 * Utiliza espaçamento horizontal dinâmico para evitar sobreposição de nós em níveis profundos.
 * O painel pode ser colocado dentro de um JScrollPane para permitir rolagem horizontal quando necessário.
 * Agora inclui cálculo de limites para evitar clipping em árvores desbalanceadas.
 */
public class BinaryTreeVisualizer extends JPanel {

    private BinaryTree tree;
    private static final int VERTICAL_GAP = 50; // Espaçamento vertical fixo entre níveis
    private static final int BASE_HORIZONTAL_GAP = 150; // Gap horizontal inicial para cálculo de tamanho preferido
    private static final int NODE_DIAMETER = 40; // Diâmetro do círculo que representa o nó
    private static final int PADDING = 20; // Padding mínimo ao redor da árvore

    public BinaryTreeVisualizer() {
        this.tree = new BinaryTree();
        setBackground(new Color(43, 45, 66)); // #2B2D42 dark background to match main UI
    }

    /**
     * Define a árvore a ser visualizada e atualiza o tamanho preferido do painel.
     * @param tree A árvore binária de busca
     */
    public void setBinaryTree(BinaryTree tree) {
        this.tree = tree;
        updatePreferredSize();
        repaint();
    }

    /**
     * Atualiza o tamanho preferido do painel com base nos limites reais da árvore.
     * Isso permite que o JScrollPane exiba barras de rolagem quando necessário.
     */
    private void updatePreferredSize() {
        if (tree == null || tree.isEmpty()) {
            setPreferredSize(new Dimension(200, 200));
            return;
        }

        // Calcula os limites da árvore com um espaçamento inicial razoável
        double initialHGAP = Math.max(BASE_HORIZONTAL_GAP, 100); // Use base gap as initial
        Bounds bounds = computeBounds(initialHGAP);

        // Largura necessária: (maxX - minX) + diâmetro do nó + 2 * padding
        int width = (int) Math.round(bounds.maxX - bounds.minX) + NODE_DIAMETER + 2 * PADDING;
        // Altura necessária: (maxY - minY) + diâmetro do nó + 2 * padding
        int height = (int) Math.round(bounds.maxY - bounds.minY) + NODE_DIAMETER + 2 * PADDING;

        setPreferredSize(new Dimension(Math.max(width, 200), Math.max(height, 200)));
    }

    /**
     * Calcula os limites (minX, maxX, minY, maxY) da árvore considerando o espaçamento horizontal inicial.
     * As coordenadas são calculadas com a raiz em (0,0) para facilitar o offset posteriormente.
     */
    private Bounds computeBounds(double initialHGAP) {
        Bounds bounds = new Bounds();
        if (tree.getRoot() != null) {
            computeBoundsRec(tree.getRoot(), 0, 0, initialHGAP, bounds);
        }
        return bounds;
    }

    private void computeBoundsRec(BinaryTree.Node node, double x, double y, double hGap, Bounds bounds) {
        if (node == null) {
            return;
        }
        // Atualiza limites com a posição atual do nó (centro)
        if (x < bounds.minX) bounds.minX = x;
        if (x > bounds.maxX) bounds.maxX = x;
        if (y < bounds.minY) bounds.minY = y;
        if (y > bounds.maxY) bounds.maxY = y;

        // Recursivamente calcula limites para filhos
        if (node.left != null) {
            computeBoundsRec(node.left, x - hGap, y + VERTICAL_GAP, hGap / 2, bounds);
        }
        if (node.right != null) {
            computeBoundsRec(node.right, x + hGap, y + VERTICAL_GAP, hGap / 2, bounds);
        }
    }

    /**
     * Classe auxiliar para armazenar os limites calculados.
     */
    private static class Bounds {
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));

        if (tree == null || tree.isEmpty()) {
            return;
        }

        BinaryTree.Node root = tree.getRoot();
        if (root == null) {
            return;
        }

        // Calcula os limites da árvore com o espaçamento inicial baseado na largura atual
        double initialHGAP = Math.max(getWidth() / 4.0, BASE_HORIZONTAL_GAP / 2);
        Bounds bounds = computeBounds(initialHGAP);

        // Calcula offset para garantir que o nó mais à esquerda fique dentro do padding
        double offsetX = -bounds.minX + PADDING;
        double offsetY = -bounds.minY + PADDING;

        // Posição inicial da raiz com offset aplicado
        double startX = offsetX; // pois a raiz estava em (0,0) no cálculo de limites
        double startY = offsetY;

        // Desenha a árvore recursivamente com o offset
        drawTree(g2d, root, startX, startY, initialHGAP);
    }

    /**
     * Desenha recursivamente a árvore a partir do nó fornecido.
     * Linhas são desenhadas primeiro, depois os nós, para que os círculos apareçam sobre as linhas.
     *
     * @param g2d     Contexto de gráficos 2D
     * @param node    Nó atual a ser desenhado
     * @param x       Coordenada X do nó atual (centro do círculo)
     * @param y       Coordenada Y do nó atual (centro do círculo)
     * @param hGap    Espaçamento horizontal a ser usado para os filhos deste nó
     */
    private void drawTree(Graphics2D g2d, BinaryTree.Node node, double x, double y, double hGap) {
        if (node == null) {
            return;
        }

        // Desenha as linhas conectando aos filhos PRIMEIRO
        // Desenha o filho esquerdo (se existir)
        if (node.left != null) {
            double leftX = x - hGap;
            double leftY = y + VERTICAL_GAP;

            // Linha conectando o nó atual ao filho esquerdo (centro a centro)
            g2d.setColor(Color.WHITE); // Linha clara para contraste no fundo escuro
            g2d.drawLine((int) Math.round(x), (int) Math.round(y), (int) Math.round(leftX), (int) Math.round(leftY));

            // Chamada recursiva para o subárvore esquerda
            drawTree(g2d, node.left, leftX, leftY, hGap / 2);
        }

        // Desenha o filho direito (se existir)
        if (node.right != null) {
            double rightX = x + hGap;
            double rightY = y + VERTICAL_GAP;

            // Linha conectando o nó atual ao filho direito (centro a centro)
            g2d.setColor(Color.WHITE); // Linha clara para contraste no fundo escuro
            g2d.drawLine((int) Math.round(x), (int) Math.round(y), (int) Math.round(rightX), (int) Math.round(rightY));

            // Chamada recursiva para o subárvore direita
            drawTree(g2d, node.right, rightX, rightY, hGap / 2);
        }

        // Depois desenha o nó atual (círculo com valor ASCII) por cima das linhas
        g2d.setColor(new Color(70, 130, 180)); // Azul aço
        g2d.fillOval((int) Math.round(x - NODE_DIAMETER / 2.0), (int) Math.round(y - NODE_DIAMETER / 2.0),
                NODE_DIAMETER, NODE_DIAMETER);
        g2d.setColor(Color.WHITE);
        // Centraliza o texto usando FontMetrics
        Font font = new Font("SansSerif", Font.BOLD, 14);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        String valueText = String.valueOf(node.valorAscii);
        int textWidth = fm.stringWidth(valueText);
        int textHeight = fm.getAscent();
        int textX = (int) Math.round(x - textWidth / 2.0);
        int textY = (int) Math.round(y + textHeight / 2.0 - fm.getDescent());
        g2d.drawString(valueText, textX, textY);
    }
}