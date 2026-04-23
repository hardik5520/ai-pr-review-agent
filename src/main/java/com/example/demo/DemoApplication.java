package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Marks this as a Spring Boot application — enables auto-configuration, component scanning, and configuration loading
@SpringBootApplication
/**
 * Entry point for the PR Review application.
 * Bootstraps and launches the Spring Boot application context.
 */
public class DemoApplication {

	// Starts the application by initializing the Spring context and embedded server
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
