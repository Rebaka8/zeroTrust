package com.zerotrust.iot.security;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class SiweVerifier {

    private static final Logger log = LoggerFactory.getLogger(SiweVerifier.class);

    public boolean verifySignature(String message, String signatureHex, String claimedAddress) {
        try {
            if (signatureHex == null || !signatureHex.startsWith("0x") || signatureHex.length() != 132) {
                log.warn("Invalid signature format: {}", signatureHex);
                return false;
            }

            byte[] signatureBytes = Hex.decode(signatureHex.substring(2));
            byte v = signatureBytes[64];
            if (v < 27) {
                v += 27;
            }

            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);

            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(
                    message.getBytes(StandardCharsets.UTF_8),
                    signatureData
            );

            String recoveredAddress = "0x" + Keys.getAddress(publicKey);
            return recoveredAddress.equalsIgnoreCase(claimedAddress);
        } catch (Exception e) {
            log.error("Failed to verify Ethereum signature: {}", e.getMessage());
            return false;
        }
    }
}
