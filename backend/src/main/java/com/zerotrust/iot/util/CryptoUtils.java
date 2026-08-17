package com.zerotrust.iot.util;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CryptoUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static String sha256Bytes(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input);
            return Hex.toHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static String keccak256(String input) {
        byte[] hash = Hash.sha3(input.getBytes(StandardCharsets.UTF_8));
        return "0x" + Hex.toHexString(hash);
    }

    public static String generateRandomNonce(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return Hex.toHexString(bytes);
    }
}
