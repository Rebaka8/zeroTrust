package com.zerotrust.iot.trust;

import org.springframework.stereotype.Component;

@Component
public class TrustCalculator {

    // Mathematical Weights (w1 + w2 + w3 + w4 = 1.0)
    public static final double WEIGHT_CRYPTO = 0.30;
    public static final double WEIGHT_BEHAVIORAL = 0.25;
    public static final double WEIGHT_FIRMWARE = 0.25;
    public static final double WEIGHT_NETWORK = 0.20;

    // Exponential Decay constant (lambda in s^-1)
    public static final double DECAY_LAMBDA = 0.0005;

    /**
     * Calculates dynamic Zero Trust score T(t):
     * T(t) = [ (w1*C + w2*B + w3*F + w4*N) * e^(-lambda * deltaSeconds) ] - Penalty
     * Output is clamped in [0, 100].
     */
    public int calculateTrustScore(
            int cryptoScore,
            int behavioralScore,
            int firmwareScore,
            int networkScore,
            long elapsedSecondsSinceLastHeartbeat,
            int penalty
    ) {
        // Weighted composite score (0 to 100)
        double composite = (WEIGHT_CRYPTO * clamp(cryptoScore))
                + (WEIGHT_BEHAVIORAL * clamp(behavioralScore))
                + (WEIGHT_FIRMWARE * clamp(firmwareScore))
                + (WEIGHT_NETWORK * clamp(networkScore));

        // Exponential decay over inactive time delta (max decay clamp to 1 hour to prevent infinite drop)
        long effectiveElapsed = Math.max(0, Math.min(elapsedSecondsSinceLastHeartbeat, 3600));
        double decayFactor = Math.exp(-DECAY_LAMBDA * effectiveElapsed);

        // Apply decay and subtract cumulative penalty
        double rawScore = (composite * decayFactor) - Math.max(0, penalty);

        return (int) Math.round(clamp(rawScore));
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
