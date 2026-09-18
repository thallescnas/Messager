package com.edc.binaryencrypt.main.test;

import com.edc.binaryencrypt.main.trees.BinaryTree;
import com.edc.binaryencrypt.main.crypto.EncryptionDecorator;

public class DebugBST {
    public static void main(String[] args) {
        String text = "teste";
        BinaryTree tree = new BinaryTree(text);
        String serialized = tree.serializePreOrder();
        System.out.println("Serialized BST (pre-order): " + serialized);
        System.out.println("Length: " + serialized.length());

        EncryptionDecorator encryptor = new EncryptionDecorator(serialized);
        String encrypted = encryptor.encrypt();
        System.out.println("Encrypted (Base64): " + encrypted);

        String decrypted = EncryptionDecorator.decrypt(encrypted);
        System.out.println("Decrypted: " + decrypted);

        BinaryTree tree2 = new BinaryTree();
        tree2.deserializePreOrder(decrypted);
        String recovered = tree2.recoverOriginalMessage();
        System.out.println("Recovered: " + recovered);
    }
}