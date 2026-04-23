package com.example.demo.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles authentication requests.
 * Exposes a single public endpoint for login.
 * Returns a JWT token on successful authentication.
 */
// Marks this as a REST controller — methods return data directly as response body
@RestController
// All endpoints in this controller are under /auth
@RequestMapping("/auth")
// Lombok: generates constructor-based dependency injection
@RequiredArgsConstructor
// Lombok: provides the log object
@Slf4j
public class AuthController {

    // JWT utility for generating tokens after successful login
    private final JwtUtil jwtUtil;

    // Hardcoded credentials — fine for a portfolio project
    // In production these would come from a database with hashed passwords
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    /**
     * Login endpoint — validates credentials and returns a JWT token.
     * This is the only way to get a token for accessing protected endpoints.
     *
     * @param request map containing "username" and "password" fields
     * @return JWT token string on success, 401 on invalid credentials
     */
    // Handles POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        // Extract username and password from the request body
        String username = request.get("username");
        String password = request.get("password");

        log.info("Login attempt for user: {}", username);

        // Validate credentials against hardcoded admin values
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {

            // Credentials are correct — generate a JWT token for this user
            String token = jwtUtil.generateToken(username);

            log.info("Login successful for user: {}", username);

            // Return the token wrapped in a JSON object
            // Frontend will read response.data.token
            return ResponseEntity.ok(Map.of("token", token));
        }

        // Credentials don't match — return 401 Unauthorized
        log.warn("Login failed for user: {}", username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
    }
}