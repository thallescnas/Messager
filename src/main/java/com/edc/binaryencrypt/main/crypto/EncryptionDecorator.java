package com.edc.binaryencrypt.main.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decorator that adds encryption to a string.
 * Uses Base64 encoding for the encrypted data.
 * Encryption: XOR each byte of UTF-8 encoded data with KEY, then Base64 encode.
 */
public class EncryptionDecorator {
    private static final int KEY = 0x5A; // Example key
    private final String data;

    public EncryptionDecorator(String data) {
        this.data = data;
    }

    /**
     * Encrypts the data using XOR with the key, then encodes in Base64.
     * @return Base64-encoded encrypted string.
     */
    public String encrypt() {
        // Convert string to bytes using UTF-8
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        // Apply XOR encryption
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) (dataBytes[i] ^ KEY);
        }
        // Encode the encrypted bytes to Base64
        return Base64.getEncoder().encodeToString(dataBytes);
    }

    /**
     * Decrypts the data from Base64, then applies XOR decryption.
     * @param encryptedData The Base64-encoded encrypted string.
     * @return Decrypted string.
     */
    public static String decrypt(String encryptedData) {
        // Decode from Base64
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        // Apply XOR decryption (same as encryption)
        for (int i = 0; i < decodedBytes.length; i++) {
            decodedBytes[i] = (byte) (decodedBytes[i] ^ KEY);
        }
        // Convert bytes to string using UTF-8
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}