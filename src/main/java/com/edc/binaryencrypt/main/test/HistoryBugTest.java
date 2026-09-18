package com.edc.binaryencrypt.main.test;

import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;
import com.edc.binaryencrypt.main.trees.BinaryTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * Test to verify the two bug reports about history corruption are fixed.
 */
public class HistoryBugTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== TESTE DE BUGS DE HISTÓRICO ===");

        // Limpar arquivos de teste de execuções anteriores
        cleanup();

        // -------- SESSÃO 1: Enviar mensagens e salvar --------
        System.out.println("\n--- Sessão 1: Enviando e salvando mensagens ---");
        Random rand1 = new Random();
        int masterKeySession1 = rand1.nextInt(); // Chave mestra da sessão 1
        System.out.println("Chave mestra da sessão 1: " + masterKeySession1);

        List<String> logSession1 = new ArrayList<>();
        String msg1 = "Olá, mundo!";
        logSession1.add("enviado: " + msg1);

        // Simular salvamento da mensagem (criptografar a BST e salvar com chave da sessão)
        String serialized1 = encryptAndSaveMessage(msg1, masterKeySession1, "msg1_sessao1.txt");
        // Simular salvamento do histórico (apenas a mensagem enviada, criptografado com masterKey da sessão)
        // Na sessão 1, o historico.enc é criado (ou limpo se existir) e então adicionamos a primeira linha.
        // Mas note: o MainWindow não limpa o historico.enc ao salvar; ele apenas append.
        // Para simular exatamente, vamos criar um novo arquivo de histórico para a sessão 1 (limpando qualquer existente) e depois adicionar.
        saveHistoryLog(logSession1, masterKeySession1, "historico.enc"); // Isso irá criar ou sobrescrever? Na verdade, estamos usando CREATE e APPEND, então se o arquivo existir, ele será append. Queremos que a sessão 1 comece limpa.
        // Vamos mudar: para a primeira sessão, vamos criar o arquivo (TRUNCATE_EXISTING) e depois para as sessões subsequentes vamos usar APPEND.
        // Para simplificar, vamos limpar o arquivo antes de cada sessão de teste? Não, porque o bug é sobre múltiplas sessões.
        // Vamos fazer: na sessão 1, vamos criar o arquivo (se não existir) e append. Se o arquivo já existir, nós estamos em uma sessão posterior.
        // Para o teste, vamos garantir que o arquivo não exista no início.
        // Já limpamos no cleanup.

        // -------- SESSÃO 2: Fechar e reabrir (nova instância), enviar mais mensagens --------
        System.out.println("\n--- Sessão 2: Nova instância, enviando mais mensagens ---");
        Random rand2 = new Random();
        int masterKeySession2 = rand2.nextInt(); // Nova chave mestra (simula nova instância do app)
        System.out.println("Chave mestra da sessão 2: " + masterKeySession2);

        List<String> logSession2 = new ArrayList<>();
        String msg2 = "Tudo bem?";
        logSession2.add("enviado: " + msg2);

        // Simular salvamento da mensagem na sessão 2
        String serialized2 = encryptAndSaveMessage(msg2, masterKeySession2, "msg2_sessao2.txt");
        // Simular salvamento do histórico na sessão 2 (apenda ao arquivo de histórico existente)
        saveHistoryLog(logSession2, masterKeySession2, "historico.enc"); // Isso irá append

        // Agora, o arquivo historico.enc contém duas linhas:
        // Linha 1: histórico da sessão 1 criptografado com masterKeySession1
        // Linha 2: histórico da sessão 2 criptografado com masterKeySession2

        // -------- TESTE 1: Corrupção de Sessões Anteriores no Histórico --------
        // Na sessão 2, tentamos carregar o histórico usando a chave de uma mensagem da sessão 1.
        // Deveríamos conseguir ver apenas o histórico da sessão 1.
        System.out.println("\n--- Teste 1: Carregando histórico da sessão 1 a partir da sessão 2 ---");
        // Carregar a chave do arquivo de mensagem da sessão 1
        Integer keyFromMsg1 = extractMasterKeyFromFile("msg1_sessao1.txt");
        if (keyFromMsg1 == null) {
            throw new IllegalStateException("Não foi possível extrair a chave do arquivo da mensagem da sessão 1");
        }
        System.out.println("Chave extraída do arquivo da mensagem da sessão 1: " + keyFromMsg1);

        // Ler o arquivo de histórico e tentar descriptografar cada linha com a chave da sessão 1
        List<String> recoveredMessages = loadHistoryWithKey("historico.enc", keyFromMsg1);
        System.out.println("Mensagens recuperadas do histórico (usando chave da sessão 1):");
        for (String m : recoveredMessages) {
            System.out.println("  " + m);
        }
        // Verificamos se a mensagem da sessão 1 está presente e a da sessão 2 não está
        boolean foundMsg1 = recoveredMessages.contains("enviado: " + msg1);
        boolean foundMsg2 = recoveredMessages.contains("enviado: " + msg2);
        if (foundMsg1 && !foundMsg2) {
            System.out.println("✅ SUCESSO: Apenas o histórico da sessão 1 foi recuperado (como esperado).");
        } else {
            System.out.println("❌ FALHA: Histórico recuperado incorretamente.");
            System.out.println("Esperado: apenas \"enviado: " + msg1 + "\"");
            System.out.println("Obtido: " + recoveredMessages);
        }

        // -------- TESTE 2: Corrupção por Mudança de Contexto (Descriptografia) --------
        // Na sessão 2, primeiro descriptografamos uma mensagem (qualquer uma), depois tentamos acessar o histórico.
        // Deveríamos ainda conseguir carregar o histórico corretamente.
        System.out.println("\n--- Teste 2: Após descriptografar uma mensagem, tentar carregar o histórico ---");
        // Primeiro, descriptografar a mensagem da sessão 2 (usando seu próprio arquivo)
        String decryptedMsg2 = decryptMessageFromFile("msg2_sessao2.txt");
        System.out.println("Mensagem da sessão 2 descriptografada: " + decryptedMsg2);
        if (!decryptedMsg2.equals(msg2)) {
            System.out.println("⚠️  Aviso: a mensagem descriptografada não corresponde à original. Isso pode indicar um problema na criptografia da mensagem.");
        } else {
            System.out.println("Mensagem da sessão 2 descriptografada corretamente.");
        }

        // Agora, usando a mesma chave da sessão 2 (que acabamos de usar para descriptografar a mensagem), tentamos carregar o histórico.
        // Nota: a chave da sessão 2 é a mesma que usamos acima (masterKeySession2).
        List<String> recoveredHistoryAfterDecrypt = loadHistoryWithKey("historico.enc", masterKeySession2);
        System.out.println("Histórico recuperado após descriptografar uma mensagem (usando chave da sessão 2):");
        for (String m : recoveredHistoryAfterDecrypt) {
            System.out.println("  " + m);
        }
        // Verificamos se a mensagem da sessão 2 está presente (e a da sessão 1 não está, porque a chave da sessão 2 não descriptografa a sessão 1)
        boolean foundMsg1After = recoveredHistoryAfterDecrypt.contains("enviado: " + msg1);
        boolean foundMsg2After = recoveredHistoryAfterDecrypt.contains("enviado: " + msg2);
        if (!foundMsg1After && foundMsg2After) {
            System.out.println("✅ SUCESSO: Após descriptografar uma mensagem, ainda podemos recuperar o histórico da sessão atual (sessão 2).");
        } else {
            System.out.println("❌ FALHA: Após descriptografar uma mensagem, o histórico ficou corrompido.");
            System.out.println("Esperado: apenas \"enviado: " + msg2 + "\" (pois a chave da sessão 2 não descriptografa a sessão 1)");
            System.out.println("Obtido: " + recoveredHistoryAfterDecrypt);
        }

        // -------- TESTE 3: Após descriptografar uma mensagem de outra sessão, tentar carregar o histórico --------
        // Agora, na sessão 2, descriptografamos uma mensagem da sessão 1 (usando seu arquivo) e depois tentamos carregar o histórico
        // usando a chave da sessão 2. Isso deveria ainda funcionar para recuperar o histórico da sessão 2.
        System.out.println("\n--- Teste 3: Após descriptografar mensagem de outra sessão, tentar carregar o histórico da sessão atual ---");
        String decryptedMsg1 = decryptMessageFromFile("msg1_sessao1.txt");
        System.out.println("Mensagem da sessão 1 descriptografada (na sessão 2): " + decryptedMsg1);
        if (!decryptedMsg1.equals(msg1)) {
            System.out.println("⚠️  Aviso: a mensagem da sessão 1 descriptografada não corresponde à original.");
        }

        // Agora, usando a chave da sessão 2, tentamos carregar o histórico.
        List<String> recoveredHistoryAfterCrossDecrypt = loadHistoryWithKey("historico.enc", masterKeySession2);
        System.out.println("Histórico recuperado após descriptografar mensagem da sessão 1 (usando chave da sessão 2):");
        for (String m : recoveredHistoryAfterCrossDecrypt) {
            System.out.println("  " + m);
        }
        boolean foundMsg1AfterCross = recoveredHistoryAfterCrossDecrypt.contains("enviado: " + msg1);
        boolean foundMsg2AfterCross = recoveredHistoryAfterCrossDecrypt.contains("enviado: " + msg2);
        if (!foundMsg1AfterCross && foundMsg2AfterCross) {
            System.out.println("✅ SUCESSO: Mesmo após descriptografar mensagem de outra sessão, o histórico da sessão atual ainda é recuperável.");
        } else {
            System.out.println("❌ FALHA: Após descriptografar mensagem de outra sessão, o histórico da sessão atual ficou corrompido.");
            System.out.println("Esperado: apenas \"enviado: " + msg2 + "\"");
            System.out.println("Obtido: " + recoveredHistoryAfterCrossDecrypt);
        }

        // Limpar arquivos de teste
        cleanup();
        System.out.println("\n=== TESTE CONCLUÍDO ===");
    }

    /** Simula o processo de salvar uma mensagem: cria BST, serializa, criptografa e salva em arquivo com chave da sessão. */
    private static String encryptAndSaveMessage(String message, int masterKey, String fileName) throws IOException {
        BinaryTree tree = new BinaryTree(message);
        String serialized = tree.serializePreOrder();
        EncryptionDecorator encryptor = new EncryptionDecorator(serialized);
        String encrypted = encryptor.encrypt();

        File file = new File(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(encrypted);
            writer.newLine();
            writer.write(Integer.toString(masterKey)); // Salva a chave mestra da sessão
        }
        return encrypted;
    }

    /** Extrai a chave mestra (segunda linha) de um arquivo de mensagem. */
    private static Integer extractMasterKeyFromFile(String fileName) throws IOException {
        File file = new File(fileName);
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (lineCount == 1) {
                    return Integer.parseInt(line);
                }
                lineCount++;
            }
        }
        return null;
    }

    /** Descriptografa a mensagem de um arquivo (primeira linha) usando a chave fixa do Decorator. */
    private static String decryptMessageFromFile(String fileName) throws Exception {
        File file = new File(fileName);
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String encrypted = reader.readLine();
            if (encrypted == null) {
                throw new IllegalStateException("Arquivo de mensagem não contém linha criptografada");
            }
            // Descriptografa usando a chave fixa do Decorator (0x5A)
            String decryptedSerialized = EncryptionDecorator.decrypt(encrypted);
            BinaryTree tree = new BinaryTree();
            tree.deserializePreOrder(decryptedSerialized);
            return tree.recoverOriginalMessage();
        }
    }

    /** Salva o log de histórico em um arquivo, cada sessão em uma nova linha, criptografado com a chave da sessão. */
    private static void saveHistoryLog(List<String> log, int masterKey, String historyFileName) throws IOException {
        // Junta todas as mensagens com quebras de linha
        String historyText = String.join(System.lineSeparator(), log);
        // Criptografa o histórico com a chave mestra da sessão
        String encryptedHistory = encryptHistoryWithKey(historyText, masterKey);
        // Adiciona ao arquivo de histórico (cada sessão em uma nova linha)
        File historyFile = new File(historyFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(historyFile.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(encryptedHistory);
            writer.newLine();
        }
    }

    /** Carrega o arquivo de histórico e tenta descriptografar cada linha com a chave fornecida,
     * retornando apenas as mensagens que começam com "enviado: " ou "recebido: ". */
    private static List<String> loadHistoryWithKey(String historyFileName, int key) throws IOException {
        List<String> recovered = new ArrayList<>();
        File historyFile = new File(historyFileName);
        if (!historyFile.exists()) {
            return recovered;
        }
        List<String> lines = Files.readAllLines(historyFile.toPath(), StandardCharsets.UTF_8);
        for (String encLine : lines) {
            if (encLine == null || encLine.isEmpty()) {
                continue;
            }
            String decrypted = decryptHistoryWithKey(encLine, key);
            if (decrypted == null || decrypted.isEmpty()) {
                continue;
            }
            // Divide o histórico descriptografado em mensagens individuais
            String[] messages = decrypted.split(System.lineSeparator());
            for (String message : messages) {
                if (message != null && !message.isEmpty() &&
                        (message.startsWith("enviado: ") || message.startsWith("recebido: "))) {
                    recovered.add(message);
                }
            }
        }
        return recovered;
    }

    /** Criptografa uma string usando XOR com a chave fornecida, então codifica em Base64.
     * Usa UTF-8 para conversão de charset. */
    private static String encryptHistoryWithKey(String plaintext, int key) {
        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < plainBytes.length; i++) {
            plainBytes[i] = (byte) (plainBytes[i] ^ key);
        }
        return Base64.getEncoder().encodeToString(plainBytes);
    }

    /** Descriptografa os dados do Base64, depois aplica a decodificação XOR com a chave fornecida.
     * Usa UTF-8 para conversão de charset. */
    private static String decryptHistoryWithKey(String encryptedData, int key) {
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        for (int i = 0; i < decodedBytes.length; i++) {
            decodedBytes[i] = (byte) (decodedBytes[i] ^ key);
        }
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /** Remove arquivos de teste. */
    private static void cleanup() {
        String[] files = {
                "msg1_sessao1.txt", "msg2_sessao2.txt",
                "historico.enc"
        };
        for (String f : files) {
            File file = new File(f);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}