package com.zerotrust.iot;

import com.zerotrust.iot.util.CryptoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZeroTrustIotApplicationTests {

    @Test
    @DisplayName("Verify SHA-256 and Keccak-256 cryptographic utilities")
    void testCryptoUtils() {
        String input = "ZERO_TRUST_DEVICE_ATTN_2026";
        String sha = CryptoUtils.sha256(input);
        assertNotNull(sha);
        assertEquals(64, sha.length());

        String keccak = CryptoUtils.keccak256(input);
        assertNotNull(keccak);
        assertTrue(keccak.startsWith("0x"));

        String nonce1 = CryptoUtils.generateRandomNonce(16);
        String nonce2 = CryptoUtils.generateRandomNonce(16);
        assertNotEquals(nonce1, nonce2);
    }
}
