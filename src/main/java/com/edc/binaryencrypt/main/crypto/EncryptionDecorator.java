package com.edc.binaryencrypt.main.crypto;

import java.util.Base64;

/**
 * Decorator that adds encryption to a string.
 * Uses Base64 encoding for the encrypted data.
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
        // Apply XOR encryption
        StringBuilder sb = new StringBuilder();
        for (char c : data.toCharArray()) {
            sb.append((char) (c ^ KEY));
        }

        // Encode the encrypted bytes to Base64
        byte[] encryptedBytes = sb.toString().getBytes();
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts the data from Base64, then applies XOR decryption.
     * @param encryptedData The Base64-encoded encrypted string.
     * @return Decrypted string.
     */
    public static String decrypt(String encryptedData) {
        // Decode from Base64
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        String decrypted = new String(decodedBytes);

        // Apply XOR decryption (same as encryption)
        StringBuilder sb = new StringBuilder();
        for (char c : decrypted.toCharArray()) {
            sb.append((char) (c ^ KEY));
        }
        return sb.toString();
    }
}