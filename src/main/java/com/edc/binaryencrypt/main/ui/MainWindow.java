package com.edc.binaryencrypt.main.ui;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * Main window of the Encrypted Messenger application using Swing.
 */
public class MainWindow extends JFrame {
    private BinaryTree currentTree;
    private final List<String> messageLog; // Stores original clear text messages for history (in memory only)
    private final DefaultListModel<String> logModel;
    private final int masterKey; // Master key for encrypting history file

    private JTextArea inputArea;
    private JList<String> logList;
    private BinaryTreeVisualizer treeVisualizer;
    private JLabel welcomeLabel;
    private JButton saveEncryptButton;

    public MainWindow() {
        this.messageLog = new ArrayList<>();
        this.logModel = new DefaultListModel<>();
        this.currentTree = new BinaryTree();
        this.masterKey = new Random().nextInt();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Messager");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Set dark theme colors
        Color bgColor = new Color(43, 45, 66); // #2B2D42
        Color panelBg = new Color(58, 63, 88); // #3A3F58
        Color textColor = new Color(200, 200, 255); // #C8C8FF
        Color accentColor = new Color(138, 149, 183); // #8A95B7
        Color buttonBg = Color.WHITE;
        Color buttonFg = new Color(51, 51, 51); // #333333

        // Central panel
        JPanel centralPanel = new JPanel(new BorderLayout(20, 20));
        centralPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centralPanel.setBackground(bgColor);
        setContentPane(centralPanel);

        // Left side: Welcome and Tree Visualizer
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(bgColor);

        welcomeLabel = new JLabel("Bem Vindo", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(accentColor);
        leftPanel.add(welcomeLabel, BorderLayout.NORTH);

        treeVisualizer = new BinaryTreeVisualizer();
        treeVisualizer.setPreferredSize(new Dimension(300, 400));
        leftPanel.add(treeVisualizer, BorderLayout.CENTER);

        // Right side: Log and Input
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(bgColor);

        // Title for log
        JLabel logTitle = new JLabel("Messager", SwingConstants.CENTER);
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        logTitle.setForeground(accentColor);
        rightPanel.add(logTitle, BorderLayout.NORTH);

        // Log list
        logList = new JList<>(logModel);
        logList.setBackground(panelBg);
        logList.setForeground(textColor);
        logList.setSelectionBackground(new Color(80, 80, 120));
        logList.setSelectionForeground(Color.WHITE);
        logList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane logScroll = new JScrollPane(logList);
        logScroll.setBorder(null);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        // Input area
        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBackground(Color.WHITE);
        inputArea.setForeground(new Color(50, 50, 50)); // Normal text color
        inputArea.setCaretColor(new Color(50, 50, 50));
        inputArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inputArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        // Set preferred size for better visibility
        inputArea.setPreferredSize(new Dimension(300, 100));
        inputArea.setMinimumSize(new Dimension(250, 80));
        // Add placeholder behavior
        inputArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (inputArea.getText().equals("Digite algo...")) {
                    inputArea.setText("");
                    inputArea.setForeground(new Color(50, 50, 50));
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (inputArea.getText().isEmpty()) {
                    inputArea.setText("Digite algo...");
                    inputArea.setForeground(new Color(150, 150, 180));
                }
            }
        });
        // Set initial placeholder
        inputArea.setText("Digite algo...");
        inputArea.setForeground(new Color(150, 150, 180));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(138, 149, 183)), // Accent color
                "Digite sua mensagem",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14),
                new Color(138, 149, 183)));
        inputScroll.setPreferredSize(new Dimension(320, 140));
        inputScroll.setMinimumSize(new Dimension(260, 120));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(bgColor);

        JButton sendButton = createButton("Enviar ->", buttonBg, buttonFg);
        saveEncryptButton = createButton("Salvar/Cript. ->", buttonBg, buttonFg);
        JButton decryptButton = createButton("Descriptografar ->", buttonBg, buttonFg);
        JButton verifyHistoryButton = createButton("Verificar Histórico", buttonBg, buttonFg);

        buttonPanel.add(sendButton);
        buttonPanel.add(saveEncryptButton);
        buttonPanel.add(decryptButton);
        buttonPanel.add(verifyHistoryButton);

        // Initially, hide save button until a message is transformed to BST
        saveEncryptButton.setVisible(false);
        // Decrypt button is always enabled (user can try to decrypt any file)
        decryptButton.setEnabled(true);

        // Bottom section: Input area and buttons stacked vertically
        JPanel bottomSection = new JPanel(new BorderLayout(10, 10));
        bottomSection.setBackground(bgColor);
        bottomSection.add(inputScroll, BorderLayout.CENTER);
        bottomSection.add(buttonPanel, BorderLayout.SOUTH);
        rightPanel.add(bottomSection, BorderLayout.SOUTH);

        // Add left and right panels to central panel
        centralPanel.add(leftPanel, BorderLayout.WEST);
        centralPanel.add(rightPanel, BorderLayout.CENTER);

        // Connect signals
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSendClicked();
            }
        });

        saveEncryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSaveEncryptClicked();
            }
        });

        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDecryptClicked();
            }
        });

        verifyHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verifyHistory();
            }
        });

        // We don't need to enable/disable decrypt button based on log selection anymore
        // logList.addListSelectionListener(e -> {
        //     if (!e.getValueIsAdjusting()) {
        //         decryptButton.setEnabled(logList.getSelectedIndex() != -1);
        //     }
        // });
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 200)));
        button.setPreferredSize(new Dimension(140, 35));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        // Hover and pressed effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().darker());
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    /**
     * Encrypts a string using XOR with the master key, then encodes in Base64.
     * Uses UTF-8 for charset conversion.
     * @param plaintext The string to encrypt.
     * @return Base64-encoded encrypted string.
     */
    private String encryptHistory(String plaintext) {
        // Convert string to bytes using UTF-8
        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        // Apply XOR encryption with master key
        for (int i = 0; i < plainBytes.length; i++) {
            plainBytes[i] = (byte) (plainBytes[i] ^ masterKey);
        }
        // Encode the encrypted bytes to Base64
        return Base64.getEncoder().encodeToString(plainBytes);
    }

    /**
     * Decrypts the data from Base64, then applies XOR decryption with the master key.
     * Uses UTF-8 for charset conversion.
     * @param encryptedData The Base64-encoded encrypted string.
     * @return Decrypted string.
     */
    private String decryptHistory(String encryptedData) {
        // Decode from Base64
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        // Apply XOR decryption with master key
        for (int i = 0; i < encryptedBytes.length; i++) {
            encryptedBytes[i] = (byte) (encryptedBytes[i] ^ masterKey);
        }
        // Convert bytes to string using UTF-8
        return new String(encryptedBytes, StandardCharsets.UTF_8);
    }

    private void onSendClicked() {
        String text = inputArea.getText();
        if (text == null || text.equals("Digite algo...") || text.isEmpty()) {
            showMessage("Digite uma mensagem antes de enviar.", "Entrada Vazia");
            return;
        }
        processInput(text); // Transform message to binary tree and update visualizer
        saveEncryptButton.setVisible(true);
        // Keep the text in the field; user can click Save/Cript. -> next
    }

    private void onSaveEncryptClicked() {
        String text = inputArea.getText();
        if (text == null || text.equals("Digite algo...") || text.isEmpty()) {
            showMessage("Digite uma mensagem antes de salvar ou criptografar.", "Entrada Vazia");
            return;
        }
        saveAndEncrypt(text);
        inputArea.setText("Digite algo...");
        inputArea.setForeground(new Color(150, 150, 180));
    }

    private void onDecryptClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar arquivo criptografado");
        fileChooser.setSelectedFile(new File("mensagem_criptografada.txt"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToDecrypt = fileChooser.getSelectedFile();
            try (BufferedReader br = Files.newBufferedReader(fileToDecrypt.toPath(), StandardCharsets.UTF_8)) {
                // Read only the first line as the encrypted message
                String encrypted = br.readLine();
                if (encrypted == null) {
                    showMessage("Arquivo de mensagem está vazio.", "Erro");
                    return;
                }

                // Decrypt to get the BST serialization
                String decryptedSerialized = EncryptionDecorator.decrypt(encrypted);
                BinaryTree tree = new BinaryTree();
                tree.deserializePreOrder(decryptedSerialized);
                currentTree = tree;
                treeVisualizer.setBinaryTree(currentTree);

                // Recover the original message from the BST
                String originalText = tree.recoverOriginalMessage();
                if (originalText != null && !originalText.isEmpty()) {
                    // We have the original text, so we can show it and add to history as received
                    messageLog.add("recebido: " + originalText);
                    updateLogList();
                    saveHistoryToFile();
                    showMessage("Mensagem original: " + originalText, "Mensagem Descriptografada");
                } else {
                    // We couldn't recover a valid message (maybe the file is corrupted or not from this system)
                    showMessage("Arquivo descriptografado com sucesso. No entanto, não pudemos recuperar a mensagem original. " +
                            "Visualize a árvore reconstruída acima.", "Arquivo Descriptografado");
                }
            } catch (IOException ex) {
                showMessage("Erro ao ler o arquivo: " + ex.getMessage(), "Erro");
            } catch (IllegalArgumentException ex) {
                showMessage("Arquivo não contém dados criptografados válidos.", "Erro");
            }
        }
    }

    /**
     * Processes the input text: builds BST from characters and updates the visualizer.
     * @param text Input text to convert to BST
     */
    public void processInput(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        currentTree = new BinaryTree(text);
        treeVisualizer.setBinaryTree(currentTree);
    }

    /**
     * Saves and encrypts the given text.
     * Shows success message upon completion.
     * @param text The original text to save and encrypt
     */
    public void saveAndEncrypt(String text) {
        if (text == null || text.isEmpty()) {
            showMessage("Digite uma mensagem antes de salvar ou criptografar.", "Entrada Vazia");
            return;
        }

        // Build BST from the text
        currentTree = new BinaryTree(text);
        // Note: we don't need to store lastMessageText anymore because we can recover from the tree if needed

        // Serialize BST to pre-order string with null markers
        String serialized = currentTree.serializePreOrder();

        // Apply encryption via Decorator
        EncryptionDecorator encryptor = new EncryptionDecorator(serialized);
        String encrypted = encryptor.encrypt();

        // Ask user where to save the encrypted message
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar mensagem criptografada");
        fileChooser.setSelectedFile(new File("mensagem_criptografada.txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try (BufferedWriter writer = Files.newBufferedWriter(fileToSave.toPath(), StandardCharsets.UTF_8)) {
                writer.write(encrypted);
                writer.newLine();
                writer.write(Integer.toString(masterKey)); // Store master key in the file
                showMessage("Mensagem criptografada salva com sucesso em: " + fileToSave.getAbsolutePath(), "Sucesso");
            } catch (IOException ex) {
                showMessage("Erro ao salvar o arquivo: " + ex.getMessage(), "Erro");
                return;
            }
        } else {
            // User cancelled the save operation
            showMessage("Operação de salvamento cancelada.", "Informação");
            return;
        }

        // For history, store as sent message
        messageLog.add("enviado: " + text);
        // Note: we no longer need encryptedToPlain for decryption lookup because we can recover from the tree

        updateLogList();

        // Save history to file (encrypted with master key)
        saveHistoryToFile();

        // Update the tree visualizer
        treeVisualizer.setBinaryTree(currentTree);
    }

    /**
     * Saves the message history to a file, encrypted with the master key.
     * Uses UTF-8 for charset conversion.
     * Appends to the history file to preserve previous sessions' history.
     * Each session's history is encrypted with its own master key and stored as a separate line.
     * Format: [encrypted_history_line]\n
     */
    private void saveHistoryToFile() {
        try {
            // Join all messages with newlines
            String historyText = String.join(System.lineSeparator(), messageLog);

            // Encrypt the history with master key
            String encryptedHistory = encryptHistory(historyText);

            // Append to history file (each session's history on a new line)
            File historyFile = new File("historico.enc");
            try (BufferedWriter writer = Files.newBufferedWriter(historyFile.toPath(), StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(encryptedHistory);
                writer.newLine();
            }
        } catch (IOException ex) {
            // Don't show error to user for history saving failures
            // as it's not critical to the main functionality
            System.err.println("Erro ao salvar histórico: " + ex.getMessage());
        }
    }

    /**
     * Clears the input field and resets the current tree.
     */
    public void clearInput() {
        inputArea.setText("Digite algo...");
        inputArea.setForeground(new Color(150, 150, 180));
        currentTree = new BinaryTree();
        treeVisualizer.setBinaryTree(currentTree);
    }

    /**
     * Updates the log list view from the messageLog.
     */
    private void updateLogList() {
        logModel.clear();
        for (String message : messageLog) {
            logModel.addElement(message);
        }
        // Scroll to the bottom
        logList.setSelectedIndex(logModel.size() - 1);
        logList.ensureIndexIsVisible(logModel.size() - 1);
    }

    /**
     * Shows a message dialog.
     * @param message The message to show
     * @param title The title of the dialog
     */
    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Loads and displays the history file by decrypting it with a key from a selected message file.
     * The history messages are shown in the history list.
     * Each line in historico.enc represents a session's entire history encrypted with that session's master key.
     * We attempt to decrypt each line with the provided key; if successful, we parse the resulting history
     * and add messages that start with "enviado: " or "recebido: ".
     */
    private void verifyHistory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar arquivo de mensagem para obter a chave");
        fileChooser.setSelectedFile(new File("mensagem_criptografada.txt"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File messageFile = fileChooser.getSelectedFile();
            try (BufferedReader br = Files.newBufferedReader(messageFile.toPath(), StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                String line;
                int lineCount = 0;
                String encryptedMessage = null;
                Integer fileMasterKey = null;
                while ((line = br.readLine()) != null) {
                    if (lineCount == 0) {
                        encryptedMessage = line;
                    } else if (lineCount == 1) {
                        try {
                            fileMasterKey = Integer.parseInt(line);
                        } catch (NumberFormatException ex) {
                            showMessage("A segunda linha do arquivo não contém uma chave válida.", "Erro");
                            return;
                        }
                    }
                    lineCount++;
                }

                if (encryptedMessage == null || fileMasterKey == null) {
                    showMessage("O arquivo de mensagem não está no formato esperado (duas linhas).", "Erro");
                    return;
                }

                // Load all lines from the history file (each line is a session's history)
                File historyFile = new File("historico.enc");
                if (!historyFile.exists()) {
                    showMessage("Arquivo de histórico não encontrado.", "Informação");
                    logModel.clear();
                    return;
                }

                List<String> historyLines;
                try {
                    historyLines = Files.readAllLines(historyFile.toPath(), StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    showMessage("Erro ao ler o arquivo de histórico: " + ex.getMessage(), "Erro");
                    return;
                }

                // Clear the current model
                logModel.clear();

                // Process each line in the history file
                for (String encSessionHistory : historyLines) {
                    if (encSessionHistory == null || encSessionHistory.isEmpty()) {
                        continue;
                    }
                    // Decrypt this session's history using the key from the selected message file
                    String decryptedSessionHistory = decryptHistoryWithKey(encSessionHistory, fileMasterKey);
                    if (decryptedSessionHistory == null || decryptedSessionHistory.isEmpty()) {
                        continue;
                    }
                    // Split the decrypted history into individual messages
                    String[] messages = decryptedSessionHistory.split(System.lineSeparator());
                    for (String message : messages) {
                        if (message == null || message.isEmpty()) {
                            continue;
                        }
                        // Only add messages that are valid history entries (they have the expected prefix)
                        if (message.startsWith("enviado: ") || message.startsWith("recebido: ")) {
                            logModel.addElement(message);
                        }
                        // Optionally, we could also add other messages, but for safety we stick to the prefix.
                    }
                }

                // Scroll to the bottom
                if (logModel.size() > 0) {
                    logList.setSelectedIndex(logModel.size() - 1);
                    logList.ensureIndexIsVisible(logModel.size() - 1);
                }

                showMessage("Histórico carregado com sucesso. Exibindo " + logModel.size() + " mensagens.", "Histórico Carregado");
            } catch (IOException ex) {
                showMessage("Erro ao ler o arquivo: " + ex.getMessage(), "Erro");
            }
        }
    }

    /**
     * Decrypts the data from Base64, then applies XOR decryption with the given key.
     * Uses UTF-8 for charset conversion.
     * @param encryptedData The Base64-encoded encrypted string.
     * @param key The XOR key to use for decryption.
     * @return Decrypted string.
     */
    private String decryptHistoryWithKey(String encryptedData, int key) {
        // Decode from Base64
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        // Apply XOR decryption with the given key
        for (int i = 0; i < decodedBytes.length; i++) {
            decodedBytes[i] = (byte) (decodedBytes[i] ^ key);
        }
        // Convert bytes to string using UTF-8
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Launches the Swing application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}