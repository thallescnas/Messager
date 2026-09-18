package com.edc.binaryencrypt.main.test;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;

public class TestDuplicates {
    public static void main(String[] args) {
        String text = "aaaa bbbb";
        System.out.println("Original text: " + text);
        System.out.println("Length: " + text.length());

        BinaryTree tree = new BinaryTree(text);
        String serialized = tree.serializePreOrder();
        System.out.println("Serialized length: " + serialized.length());

        // Encrypt the serialized string
        EncryptionDecorator encryptor = new EncryptionDecorator(serialized);
        String encrypted = encryptor.encrypt();
        System.out.println("Encrypted: " + encrypted);

        // Decrypt
        String decryptedSerialized = EncryptionDecorator.decrypt(encrypted);
        System.out.println("Decrypted serialized length: " + decryptedSerialized.length());

        // Rebuild tree
        BinaryTree tree2 = new BinaryTree();
        tree2.deserializePreOrder(decryptedSerialized);
        String recovered = tree2.recoverOriginalMessage();
        System.out.println("Recovered: " + recovered);
        System.out.println("Recovered length: " + recovered.length());

        if (text.equals(recovered)) {
            System.out.println("✅ SUCCESS: Text recovered correctly.");
        } else {
            System.out.println("❌ FAILURE: Text not recovered correctly.");
        }
    }
}