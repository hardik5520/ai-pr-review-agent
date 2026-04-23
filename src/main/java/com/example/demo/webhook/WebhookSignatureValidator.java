package com.example.demo.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

// Registers this class as a Spring-managed component so it can be injected into other beans
@Component
/**
 * Validates the HMAC-SHA256 signature on incoming GitHub webhook requests.
 * GitHub signs every webhook payload with a shared secret; this class verifies that signature
 * to ensure the request is genuinely from GitHub and has not been tampered with.
 */
public class WebhookSignatureValidator {

    // Injects the webhook secret from application properties (github.webhook-secret)
    @Value("${github.webhook-secret}")
    private String webhookSecret;

    /**
     * Validates the incoming webhook by comparing the provided signature against
     * an HMAC-SHA256 signature computed from the raw request payload.
     * Throws a 403 Forbidden error if the signature is missing or does not match.
     */
    public void validate(byte[] payload, String signatureHeader) {
        // Reject the request immediately if no signature header was sent
        if (signatureHeader == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing signature header");
        }

        // Build the expected signature string in the format GitHub uses: "sha256=<hex>"
        String expected = "sha256=" + computeHmac(payload);

        // Use a constant-time comparison to prevent timing attacks when checking equality
        if (!MessageDigest.isEqual(expected.getBytes(), signatureHeader.getBytes())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid signature");
        }
    }

    /**
     * Computes an HMAC-SHA256 hash of the given payload using the configured webhook secret.
     * Returns the result as a lowercase hexadecimal string.
     */
    private String computeHmac(byte[] payload) {
        try {
            // Initialize the HMAC-SHA256 algorithm
            Mac mac = Mac.getInstance("HmacSHA256");
            // Configure the MAC with the secret key
            mac.init(new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256"));
            // Compute the hash of the payload
            byte[] hash = mac.doFinal(payload);

            // Convert each byte of the hash to a two-character hex string
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }
}
