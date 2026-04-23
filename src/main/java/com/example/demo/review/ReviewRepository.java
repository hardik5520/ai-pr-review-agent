package com.example.demo.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Marks this interface as a Spring-managed repository bean so it can be injected elsewhere
@Repository
/**
 * Data access interface for ReviewEntity records.
 * Extends JpaRepository to inherit standard database operations such as save, find, and delete
 * without requiring any custom implementation.
 */
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
}
