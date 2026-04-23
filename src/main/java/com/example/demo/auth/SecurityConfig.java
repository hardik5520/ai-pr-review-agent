package com.example.demo.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 * Defines which endpoints are public, which require authentication,
 * wires in the JWT filter, disables sessions, and configures CORS.
 */
// Marks this as a Spring configuration class
@Configuration
// Enables Spring Security's web security support
@EnableWebSecurity
// Lombok: generates constructor-based dependency injection
@RequiredArgsConstructor
public class SecurityConfig {

    // Our custom JWT filter that validates tokens on every request
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Defines the security filter chain — the core security configuration.
     * Sets up which endpoints are public, which need auth,
     * disables CSRF and sessions, and registers our JWT filter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs
            // CSRF protects session-based apps, JWT handles its own security
            .csrf(csrf -> csrf.disable())

            // Configure CORS — allows React frontend (port 5173) to call our API (port 8080)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Define which endpoints are public and which require authentication
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints — no token needed
                    .requestMatchers("/webhook/github").permitAll()
                    .requestMatchers("/auth/login").permitAll()
                    // Prometheus scrapes this endpoint automatically — no JWT available
                    .requestMatchers("/actuator/prometheus", "/actuator/health").permitAll()
                    // Everything else requires a valid JWT token
                    .anyRequest().authenticated()
            )

            // Use stateless sessions — Spring will NOT create HTTP sessions
            // Every request must carry its own JWT token
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our JWT filter BEFORE Spring's built-in username/password filter
            // This ensures JWT is checked first on every request
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration — Cross Origin Resource Sharing.
     * Allows the React frontend running on localhost:5173
     * to make API calls to Spring Boot on localhost:8080.
     * Without this the browser blocks all cross-origin requests.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from React dev server
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "https://ai-pr-review-agent.vercel.app"
        ));

        // Allow these HTTP methods from the frontend
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow these headers in requests — Authorization is needed for JWT
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Apply this CORS config to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}