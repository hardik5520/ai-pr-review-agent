package com.example.demo.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// Marks this interface as a Spring-managed repository bean so it can be injected elsewhere
@Repository
/**
 * Data access interface for PullRequestEntity records.
 * Extends JpaRepository to inherit standard database operations such as save, find, and delete
 * without requiring any custom implementation.
 */
public interface PullRequestRepository extends JpaRepository<PullRequestEntity, Long> {

    // Finds the most recent PR record by repo name and PR number — used by PostCommentNode
    Optional<PullRequestEntity> findFirstByRepoNameAndPrNumberOrderByCreatedAtDesc(String repoName, Integer prNumber);

    /**
     * Returns the number of pull requests grouped by author — used by the Trends page.
     *
     * @Query lets us write JPQL (JPA Query Language) directly when Spring Data's
     * method-name conventions aren't expressive enough.
     *
     * JPQL looks like SQL but operates on Java class/field names, not table/column names.
     * "p.author" refers to the `author` field on PullRequestEntity, not the DB column.
     *
     * The result is a List of Object[] arrays where:
     *   index 0 = author (String)
     *   index 1 = count (Long)
     *
     * The controller maps these into TrendDto records.
     */
    @Query("SELECT p.author, COUNT(p) FROM PullRequestEntity p GROUP BY p.author ORDER BY COUNT(p) DESC")
    List<Object[]> countPrsByAuthor();
}
