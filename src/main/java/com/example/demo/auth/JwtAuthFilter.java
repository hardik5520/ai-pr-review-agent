package com.example.demo.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter that runs on every incoming HTTP request.
 * Reads the JWT token from the Authorization header, validates it,
 * and sets the authenticated user in Spring Security's context.
 * Extends OncePerRequestFilter to guarantee it runs exactly once per request.
 */
// Marks this as a Spring-managed component
@Component
// Lombok: generates constructor-based dependency injection
@RequiredArgsConstructor
// Lombok: provides the log object for logging
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // JWT utility for token validation and username extraction
    private final JwtUtil jwtUtil;

    /**
     * Core filter method — runs on every request.
     * Extracts and validates the JWT token, then sets authentication
     * in Spring's SecurityContext if the token is valid.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Read the Authorization header from the request
        String authHeader = request.getHeader("Authorization");

        // If no Authorization header or it doesn't start with "Bearer ", skip JWT processing
        // The request will be handled by Spring Security as unauthenticated
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token by removing the "Bearer " prefix (7 characters)
        String token = authHeader.substring(7);
        String username = null;

        try {
            // Extract username from the token — also validates the signature internally
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            // Token is malformed, expired, or has invalid signature
            log.warn("JWT validation failed: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Only set authentication if we got a username AND no authentication exists yet for this request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate the token against the extracted username
            if (jwtUtil.validateToken(token, username)) {

                // Create an authentication object with the username
                // Third parameter is authorities/roles — empty list for now (no role-based access)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, List.of());

                // Attach request details (IP address, session ID) to the authentication
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store the authentication in Spring Security's context for this request
                // This tells Spring Security "this request is authenticated as username"
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authenticated request for user: {}", username);
            }
        }

        // Continue the filter chain — pass request to the next filter or controller
        filterChain.doFilter(request, response);
    }
}