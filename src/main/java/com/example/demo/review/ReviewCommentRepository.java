package com.example.demo.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Marks this interface as a Spring-managed repository bean so it can be injected elsewhere
@Repository
/**
 * Data access interface for ReviewCommentEntity records.
 * Extends JpaRepository to inherit standard database operations such as save, find, and delete
 * without requiring any custom implementation.
 */
public interface ReviewCommentRepository extends JpaRepository<ReviewCommentEntity, Long> {

    /**
     * Finds all comment rows that belong to a specific review.
     * Spring Data derives the SQL automatically from the method name:
     *   "findBy" + "Review" → WHERE review_id = :review
     * Used by ReviewController to load comments for the detail page.
     */
    List<ReviewCommentEntity> findByReview(ReviewEntity review);
}
