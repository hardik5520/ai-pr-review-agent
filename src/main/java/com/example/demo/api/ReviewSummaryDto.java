package com.example.demo.api;

import java.time.LocalDateTime;

/**
 * A lightweight snapshot of one review — used to populate the main dashboard table.
 * We use a DTO (Data Transfer Object) instead of returning the raw JPA entity directly
 * because:
 *   1. JPA entities with lazy-loaded relationships can cause serialization errors (LazyInitializationException)
 *   2. We want to control exactly which fields are exposed over the API
 *   3. We can flatten nested objects (e.g. pull request fields) into a single flat response
 *
 * Java 16+ record syntax gives us an immutable class with constructor, getters,
 * equals, hashCode, and toString for free — no Lombok needed.
 */
public record ReviewSummaryDto(

        // Primary key of the review record
        Long id,

        // The GitHub repo this review belongs to (e.g. "hardik/my-repo")
        String repoName,

        // The PR number within that repo
        Integer prNumber,

        // Title of the pull request
        String prTitle,

        // GitHub username of the person who opened the PR
        String author,

        // Overall severity determined by the AI: INFO, WARNING, or CRITICAL
        String overallSeverity,

        // Which AI model produced this review (e.g. "gpt-4o")
        String modelUsed,

        // How long the full agent graph took to run, in milliseconds
        Long latencyMs,

        // When this review record was saved to the database
        LocalDateTime createdAt
) {}
