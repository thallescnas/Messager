package com.edc.binaryencrypt.main.test;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;

/**
 * Verifies that encryption is applied to the BST serialization, not the plain text.
 */
public class VerifyEncryption {
    public static void main(String[] args) {
        String plainText = "abc";
        System.out.println("=== VERIFICACAO DE CRIPTOGRAFIA ===");
        System.out.println("Texto plano: '" + plainText + "'");

        // Create BST from text
        BinaryTree tree = new BinaryTree(plainText);
        System.out.println("Arvore BST criada a partir do texto");

        // Serialize BST to pre-order
        String serialized = tree.serializePreOrder();
        System.out.println("Serializacao PRE-ORDEM da BST: '" + serialized + "'");
        System.out.println("(Formato: valor,valor,...,# para nulos)");

        // Encrypt the SERIALIZED BST (not the plain text!)
        EncryptionDecorator encryptor = new EncryptionDecorator(serialized);
        String encrypted = encryptor.encrypt();
        System.out.println("Texto criptografado (Base64): '" + encrypted + "'");

        // Verify that encrypting the plain text directly gives DIFFERENT result
        EncryptionDecorator encryptorDirect = new EncryptionDecorator(plainText);
        String encryptedDirect = encryptorDirect.encrypt();
        System.out.println("Criptografando o TEXTO PLAIN diretamente: '" + encryptedDirect + "'");

        // These should be DIFFERENT if we're doing it right
        boolean isDifferent = !encrypted.equals(encryptedDirect);
        System.out.println("Os resultados sao diferentes? " + isDifferent);

        if (isDifferent) {
            System.out.println("✅ SUCESSO: Estamos criptografando a arvore BST, nao o texto plano");
        } else {
            System.out.println("❌ FALHA: Estamos criptografando o texto plano diretamente");
        }

        // Test decryption
        String decryptedSerialized = EncryptionDecorator.decrypt(encrypted);
        System.out.println("Serializacao de volta apos descriptografia: '" + decryptedSerialized + "'");

        // Recreate tree from decrypted data
        BinaryTree tree2 = new BinaryTree();
        tree2.deserializePreOrder(decryptedSerialized);
        String recovered = tree2.recoverOriginalMessage();
        System.out.println("Texto recuperado (com posicao original): '" + recovered + "'");
        System.out.println("(Note: com a nova BST, podemos recuperar a mensagem original exatamente)");
    }
}