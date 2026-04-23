package com.example.demo.review;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Marks this class as a JPA entity so it maps to a database table
@Entity
// Specifies that this entity maps to the "pull_requests" table
@Table(name = "pull_requests")
// Lombok: generates getters, setters, equals, hashCode, and toString automatically
@Data
// Lombok: enables the builder pattern for creating instances (e.g., PullRequestEntity.builder()...)
@Builder
// Lombok: generates a no-argument constructor required by JPA
@NoArgsConstructor
// Lombok: generates a constructor that accepts all fields
@AllArgsConstructor
/**
 * Represents a pull request received from GitHub.
 * Stores key details such as the repository name, PR number, author, title, and current status.
 */
public class PullRequestEntity {

    // Marks this field as the primary key for the table
    @Id
    // Tells the database to auto-generate the ID value on insert
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Full name of the repository (e.g., "owner/repo")
    private String repoName;

    // The pull request number within the repository
    private Integer prNumber;

    // GitHub username of the person who opened the PR
    private String author;

    // Title of the pull request
    private String title;

    // Current processing status of the PR (e.g., "PENDING", "REVIEWED")
    private String status;

    // Timestamp of when this record was created
    private LocalDateTime createdAt;
}
