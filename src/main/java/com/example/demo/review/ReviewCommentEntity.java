package com.example.demo.review;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Marks this class as a JPA entity so it maps to a database table
@Entity
// Specifies that this entity maps to the "review_comments" table
@Table(name = "review_comments")
// Lombok: generates getters, setters, equals, hashCode, and toString automatically
@Data
// Lombok: enables the builder pattern for creating instances
@Builder
// Lombok: generates a no-argument constructor required by JPA
@NoArgsConstructor
// Lombok: generates a constructor that accepts all fields
@AllArgsConstructor
/**
 * Represents a single inline comment left by the AI reviewer on a specific line of a file.
 * Each comment belongs to a review and targets a particular file and line number in the pull request diff.
 */
public class ReviewCommentEntity {

    // Marks this field as the primary key for the table
    @Id
    // Tells the database to auto-generate the ID value on insert
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links this comment to the review it belongs to; loaded lazily to avoid unnecessary DB queries
    @ManyToOne(fetch = FetchType.LAZY)
    // Specifies the foreign key column name in the "review_comments" table
    @JoinColumn(name = "review_id")
    private ReviewEntity review;

    // Relative path of the file in the repository that this comment applies to
    private String filePath;

    // Line number within the file where the issue was identified
    private Integer lineNumber;

    // Severity level of this specific comment (e.g., "LOW", "MEDIUM", "HIGH")
    private String severity;

    // The actual text of the review comment written by the AI
    // TEXT type used because comment text can exceed the default VARCHAR(255) limit
    @Column(columnDefinition = "TEXT")
    private String commentText;

    // Timestamp of when this comment record was created
    private LocalDateTime createdAt;
}
