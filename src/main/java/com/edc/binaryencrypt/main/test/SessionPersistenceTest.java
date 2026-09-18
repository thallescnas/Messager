package com.edc.binaryencrypt.main.test;

import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;
import com.edc.binaryencrypt.main.trees.BinaryTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class SessionPersistenceTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== TESTE DE PERSISTÊNCIA ENTRE SESSÕES ===");

        // -------- SESSÃO 1: Enviar, salvar e criptografar uma mensagem --------
        System.out.println("\n--- Sessão 1: Enviando e salvando mensagem ---");
        Random random1 = new Random();
        int masterKeySession1 = random1.nextInt(); // Chave mestra da sessão 1
        System.out.println("Chave mestra da sessão 1: " + masterKeySession1);

        String originalMessage = "teste";
        System.out.println("Mensagem original: " + originalMessage);

        // Passo A: Construir BST a partir da mensagem original
        BinaryTree tree = new BinaryTree(originalMessage);
        System.out.println("BST construída a partir da mensagem");

        // Passo B: Serializar a BST em pré-ordem
        String serializedBST = tree.serializePreOrder();
        System.out.println("Serialização PRE-ORDEM da BST: " + serializedBST);

        // Passo C: Criptografar a serialização (usando a chave fixa do Decorator)
        EncryptionDecorator encryptor = new EncryptionDecorator(serializedBST);
        String encryptedMessage = encryptor.encrypt();
        System.out.println("Mensagem criptografada (Base64 da serialização criptografada): " + encryptedMessage);

        // Salvar a mensagem em um arquivo (simulando o que o MainWindow faz)
        File messageFile = new File("mensagem_teste.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(messageFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(encryptedMessage);
            writer.newLine();
            writer.write(Integer.toString(masterKeySession1)); // Salva a chave mestra da sessão
        }
        System.out.println("Mensagem salva em: " + messageFile.getAbsolutePath());

        // Simular salvamento do histórico (apenas a mensagem enviada)
        List<String> historyLog = new ArrayList<>();
        historyLog.add("enviado: " + originalMessage);
        String historyText = String.join(System.lineSeparator(), historyLog);
        String encryptedHistory = encryptHistoryWithKey(historyText, masterKeySession1);
        File historyFile = new File("historico_teste.enc");
        try (BufferedWriter writer = Files.newBufferedWriter(historyFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(encryptedHistory);
        }
        System.out.println("Histórico salvo em: " + historyFile.getAbsolutePath());

        // -------- SESSÃO 2: Fechar e reabrir o app, então tentar descriptografar --------
        System.out.println("\n--- Sessão 2: Tentando descriptografar o arquivo salvo ---");
        Random random2 = new Random();
        int masterKeySession2 = random2.nextInt(); // Nova chave mestra (simula nova instância do app)
        System.out.println("Chave mestra da sessão 2 (não deveria ser usada para descriptografar a mensagem): " + masterKeySession2);

        // Ler o arquivo de mensagem salvo
        String encryptedFromFile = null;
        Integer masterKeyFromFile = null;
        try (BufferedReader reader = Files.newBufferedReader(messageFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (lineCount == 0) {
                    encryptedFromFile = line;
                } else if (lineCount == 1) {
                    masterKeyFromFile = Integer.parseInt(line);
                }
                lineCount++;
            }
        }
        if (encryptedFromFile == null || masterKeyFromFile == null) {
            throw new IllegalStateException("Arquivo de mensagem não está no formato esperado.");
        }
        System.out.println("Lido do arquivo - Mensagem criptografada: " + encryptedFromFile);
        System.out.println("Lido do arquivo - Chave mestra (da sessão 1): " + masterKeyFromFile);

        // Passo D: Descriptografar a mensagem (usa a chave fixa 0x5A do Decorator, não a chave do arquivo)
        String decryptedSerializedBST = EncryptionDecorator.decrypt(encryptedFromFile);
        System.out.println("Mensagem descriptografada (serialização da BST): " + decryptedSerializedBST);

        // Passo E: Reconstruir a BST a partir da serialização
        tree = new BinaryTree();
        tree.deserializePreOrder(decryptedSerializedBST);
        String recoveredMessage = tree.recoverOriginalMessage();
        System.out.println("Mensagem recuperada da BST: " + recoveredMessage);

        // Verificar se a mensagem recuperada corresponde à original
        if (originalMessage.equals(recoveredMessage)) {
            System.out.println("✅ SUCESSO: A mensagem foi recuperada corretamente entre sessões!");
        } else {
            System.out.println("❌ FALHA: A mensagem recuperada não corresponde à original.");
            System.out.println("Esperado: " + originalMessage);
            System.out.println("Obtido: " + recoveredMessage);
        }

        // -------- TESTE DO HISTÓRICO --------
        System.out.println("\n--- Testando recuperação do histórico ---");
        // Ler o arquivo de histórico salvo na sessão 1
        String encryptedHistoryFromFile;
        try (BufferedReader reader = Files.newBufferedReader(historyFile.toPath(), StandardCharsets.UTF_8)) {
            encryptedHistoryFromFile = reader.readLine();
        }
        System.out.println("Histórico criptografado lido do arquivo: " + encryptedHistoryFromFile);

        // Descriptografar o histórico usando a chave mestra do arquivo de mensagem (que é a chave da sessão 1)
        String decryptedHistory = decryptHistoryWithKey(encryptedHistoryFromFile, masterKeyFromFile);
        System.out.println("Histórico descriptografado: " + decryptedHistory);

        if (historyText.equals(decryptedHistory)) {
            System.out.println("✅ SUCESSO: O histórico foi recuperado corretamente entre sessões!");
        } else {
            System.out.println("❌ FALHA: O histórico recuperado não corresponde ao original.");
            System.out.println("Esperado: " + historyText);
            System.out.println("Obtido: " + decryptedHistory);
        }

        // Limpar arquivos de teste
        messageFile.delete();
        historyFile.delete();
    }

    /**
     * Criptografa uma string usando XOR com a chave fornecida, então codifica em Base64.
     * Usa UTF-8 para conversão de charset.
     */
    private static String encryptHistoryWithKey(String plaintext, int key) {
        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < plainBytes.length; i++) {
            plainBytes[i] = (byte) (plainBytes[i] ^ key);
        }
        return Base64.getEncoder().encodeToString(plainBytes);
    }

    /**
     * Descriptografa os dados do Base64, depois aplica a decodificação XOR com a chave fornecida.
     * Usa UTF-8 para conversão de charset.
     * @param encryptedData A string criptografada em Base64.
     * @param key A chave XOR a ser usada para decodificação.
     * @return String decodificada.
     */
    private static String decryptHistoryWithKey(String encryptedData, int key) {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        for (int i = 0; i < encryptedBytes.length; i++) {
            encryptedBytes[i] = (byte) (encryptedBytes[i] ^ key);
        }
        return new String(encryptedBytes, StandardCharsets.UTF_8);
    }
}