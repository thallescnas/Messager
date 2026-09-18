package com.edc.binaryencrypt.main.test;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;

/**
 * Test to verify that encryption is applied to the serialized BST, not the plain text.
 */
public class TestEncryption {
    public static void main(String[] args) {
        String text = "hello";
        System.out.println("Original text: " + text);

        BinaryTree tree = new BinaryTree(text);
        String serialized = tree.serializePreOrder();
        System.out.println("Serialized BST (pre-order): " + serialized);

        EncryptionDecorator encryptor = new EncryptionDecorator(serialized);
        String encrypted = encryptor.encrypt();
        System.out.println("Encrypted (Base64): " + encrypted);

        // Decrypt to verify
        String decryptedSerialized = EncryptionDecorator.decrypt(encrypted);
        System.out.println("Decrypted serialized: " + decryptedSerialized);

        // Rebuild tree from decrypted serialized
        BinaryTree tree2 = new BinaryTree();
        tree2.deserializePreOrder(decryptedSerialized);
        String recovered = tree2.recoverOriginalMessage();
        System.out.println("Recovered original message: " + recovered);
        // Note: with the new BST, we can recover the exact original message
    }
}