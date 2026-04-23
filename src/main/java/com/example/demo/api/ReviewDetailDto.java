package com.example.demo.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full details for a single review — used by the review detail page.
 * Includes the complete AI-generated review text plus every inline comment
 * that was parsed and saved from that review.
 *
 * Uses a nested record (CommentDto) to represent each comment row.
 * Nesting records inside a record is clean and keeps related shapes together.
 */
public record ReviewDetailDto(

        // Primary key of the review record
        Long id,

        // The GitHub repo this review belongs to
        String repoName,

        // The PR number within that repo
        Integer prNumber,

        // Title of the pull request
        String prTitle,

        // GitHub username of the person who opened the PR
        String author,

        // Full AI-generated review text (can be thousands of characters)
        String summary,

        // Overall severity: INFO, WARNING, or CRITICAL
        String overallSeverity,

        // AI model that produced this review
        String modelUsed,

        // Time the agent graph took to complete, in milliseconds
        Long latencyMs,

        // When this review was saved
        LocalDateTime createdAt,

        // All inline comments parsed from the review text
        List<CommentDto> comments
) {

    /**
     * Represents one parsed comment line from the review.
     * Each comment corresponds to a single 🔴 / 🟡 / 🔵 line
     * that was extracted and saved to the review_comments table.
     */
    public record CommentDto(

            // Primary key of the comment record
            Long id,

            // File this comment applies to ("general" if not file-specific)
            String filePath,

            // Line number in the file (0 if not file-specific)
            Integer lineNumber,

            // Severity of this specific comment: INFO, WARNING, or CRITICAL
            String severity,

            // The full text of the comment line
            String commentText,

            // When this comment was saved
            LocalDateTime createdAt
    ) {}
}
