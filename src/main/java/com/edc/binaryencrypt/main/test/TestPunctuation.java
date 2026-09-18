package com.edc.binaryencrypt.main.test;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;

public class TestPunctuation {
    public static void main(String[] args) {
        String text = "Hello, World! How are you? I'm fine; thanks.";
        System.out.println("Original text: " + text);

        BinaryTree tree = new BinaryTree(text);
        String serialized = tree.serializePreOrder();
        System.out.println("Serialized: " + serialized);

        // Encrypt the serialized string
        EncryptionDecorator encryptor = new EncryptionDecorator(serialized);
        String encrypted = encryptor.encrypt();
        System.out.println("Encrypted: " + encrypted);

        // Decrypt
        String decryptedSerialized = EncryptionDecorator.decrypt(encrypted);
        System.out.println("Decrypted serialized: " + decryptedSerialized);

        // Rebuild tree
        BinaryTree tree2 = new BinaryTree();
        tree2.deserializePreOrder(decryptedSerialized);
        String recovered = tree2.recoverOriginalMessage();
        System.out.println("Recovered: " + recovered);

        if (text.equals(recovered)) {
            System.out.println("✅ SUCCESS: Text recovered correctly.");
        } else {
            System.out.println("❌ FAILURE: Text not recovered correctly.");
            System.out.println("Expected length: " + text.length());
            System.out.println("Recovered length: " + recovered.length());
        }
    }
}