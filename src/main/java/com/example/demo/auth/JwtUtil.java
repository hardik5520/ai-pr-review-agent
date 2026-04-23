package com.example.demo.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for all JWT operations.
 * Handles generating tokens when user logs in
 * and validating tokens on every incoming request.
 */
// Marks this as a Spring-managed component so it can be injected anywhere
@Component
public class JwtUtil {

    // Secret key string injected from application.yml
    @Value("${jwt.secret}")
    private String secret;

    // Token expiry time in milliseconds injected from application.yml
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Converts the plain secret string into a cryptographic signing key.
     * HMAC-SHA256 requires the key to be at least 256 bits (32 bytes).
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a JWT token for a given username.
     * Called when user successfully logs in.
     * The token contains the username and expiry time, signed with our secret.
     *
     * @param username the authenticated user's name
     * @return signed JWT token string e.g. "eyJhbGci..."
     */
    public String generateToken(String username) {
        return Jwts.builder()
                // Set the subject (who this token is for)
                .setSubject(username)
                // Set when the token was created
                .setIssuedAt(new Date())
                // Set when the token expires
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                // Sign the token with our secret key using HMAC-SHA256
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     * Called on every request to identify who is making the call.
     *
     * @param token the JWT token string
     * @return the username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Checks if a token is valid — correct signature and not expired.
     *
     * @param token    the JWT token string
     * @param username the username to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        // Token is valid if username matches and it hasn't expired
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    /**
     * Parses the token and returns all claims (the payload data inside the token).
     * Throws an exception if the token signature is invalid or tampered with.
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the token's expiry date is before the current time.
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}