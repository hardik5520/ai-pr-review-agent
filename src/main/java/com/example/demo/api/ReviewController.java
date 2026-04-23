package com.example.demo.api;

import com.example.demo.review.PullRequestRepository;
import com.example.demo.review.ReviewCommentRepository;
import com.example.demo.review.ReviewEntity;
import com.example.demo.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Marks this class as a REST controller — methods return JSON response bodies directly
@RestController
// All endpoints in this controller are prefixed with /api
@RequestMapping("/api")
// Lombok: generates constructor-based dependency injection for all final fields
@RequiredArgsConstructor
// Lombok: injects a logger so we can write log.info(...) etc.
@Slf4j
/**
 * REST API controller that exposes review data for the React dashboard.
 *
 * Three endpoints:
 *   GET /api/reviews        → list of all reviews (dashboard table)
 *   GET /api/reviews/{id}   → single review with comments (detail page)
 *   GET /api/trends         → per-author PR count (bar chart)
 *
 * All endpoints are protected by JWT — the JwtAuthFilter runs before every
 * request and blocks unauthenticated callers with a 401.
 */
@CrossOrigin(origins = {"http://localhost:5173", "https://ai-pr-review-agent.vercel.app"})
// Allows the React dev server (Vite default port 5173) to call these endpoints.
// Without this, browsers block cross-origin requests (CORS policy).
// In production this would be locked to the real frontend domain.
public class ReviewController {

    // Access review records saved by PostCommentNode
    private final ReviewRepository reviewRepository;

    // Access inline comment rows saved by PostCommentNode
    private final ReviewCommentRepository reviewCommentRepository;

    // Access pull request records saved by WebhookController
    private final PullRequestRepository pullRequestRepository;

    /**
     * Returns a summary list of all reviews — one row per review.
     * The dashboard table calls this endpoint on page load.
     *
     * Response shape: array of ReviewSummaryDto JSON objects.
     *
     * Example response:
     * [
     *   {
     *     "id": 1,
     *     "repoName": "hardik/my-repo",
     *     "prNumber": 7,
     *     "prTitle": "Add login feature",
     *     "author": "hardik",
     *     "overallSeverity": "WARNING",
     *     "modelUsed": "gpt-4o",
     *     "latencyMs": 4200,
     *     "createdAt": "2024-01-15T10:30:00"
     *   }
     * ]
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewSummaryDto>> getAllReviews() {
        log.info("GET /api/reviews — fetching all reviews");

        // Load all review rows from the database
        List<ReviewEntity> reviews = reviewRepository.findAll();

        // Map each ReviewEntity to a ReviewSummaryDto.
        // We access review.getPullRequest() here — this triggers a JOIN to pull_requests
        // because the relationship is FetchType.LAZY (loaded only when accessed).
        // That's fine inside a @Transactional context (Spring Data ensures one exists here).
        List<ReviewSummaryDto> result = reviews.stream()
                .map(review -> new ReviewSummaryDto(
                        review.getId(),
                        review.getPullRequest().getRepoName(),
                        review.getPullRequest().getPrNumber(),
                        review.getPullRequest().getTitle(),
                        review.getPullRequest().getAuthor(),
                        review.getOverallSeverity(),
                        review.getModelUsed(),
                        review.getLatencyMs(),
                        review.getCreatedAt()
                ))
                .toList();

        log.info("GET /api/reviews — returning {} reviews", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Returns full details of one review including all its inline comments.
     * The review detail page (/review/:id) calls this endpoint.
     *
     * @param id the database primary key of the review record
     * @return 200 with ReviewDetailDto, or 404 if no review exists with that id
     *
     * Example response:
     * {
     *   "id": 1,
     *   "repoName": "hardik/my-repo",
     *   "prNumber": 7,
     *   "prTitle": "Add login feature",
     *   "author": "hardik",
     *   "summary": "## Code Review\n🔴 CRITICAL: ...",
     *   "overallSeverity": "WARNING",
     *   "modelUsed": "gpt-4o",
     *   "latencyMs": 4200,
     *   "createdAt": "2024-01-15T10:30:00",
     *   "comments": [
     *     { "id": 1, "filePath": "general", "lineNumber": 0, "severity": "WARNING", ... }
     *   ]
     * }
     */
    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewDetailDto> getReviewById(@PathVariable Long id) {
        log.info("GET /api/reviews/{} — fetching review detail", id);

        // findById returns an Optional — we return 404 if nothing is found
        return reviewRepository.findById(id)
                .map(review -> {

                    // Load all comment rows that belong to this review
                    var comments = reviewCommentRepository.findByReview(review)
                            .stream()
                            .map(c -> new ReviewDetailDto.CommentDto(
                                    c.getId(),
                                    c.getFilePath(),
                                    c.getLineNumber(),
                                    c.getSeverity(),
                                    c.getCommentText(),
                                    c.getCreatedAt()
                            ))
                            .toList();

                    // Build the full detail DTO
                    ReviewDetailDto dto = new ReviewDetailDto(
                            review.getId(),
                            review.getPullRequest().getRepoName(),
                            review.getPullRequest().getPrNumber(),
                            review.getPullRequest().getTitle(),
                            review.getPullRequest().getAuthor(),
                            review.getSummary(),
                            review.getOverallSeverity(),
                            review.getModelUsed(),
                            review.getLatencyMs(),
                            review.getCreatedAt(),
                            comments
                    );

                    log.info("GET /api/reviews/{} — found review with {} comments", id, comments.size());
                    return ResponseEntity.ok(dto);
                })
                // If no review found with that id, return HTTP 404 Not Found
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns per-author PR counts for the trends bar chart.
     * The trends page (/trends) calls this to know how many PRs each contributor opened.
     *
     * Response shape: array of { author, prCount } objects, sorted by count descending.
     *
     * Example response:
     * [
     *   { "author": "hardik", "prCount": 12 },
     *   { "author": "alice",  "prCount": 5  }
     * ]
     */
    @GetMapping("/trends")
    public ResponseEntity<List<TrendDto>> getTrends() {
        log.info("GET /api/trends — fetching author trend data");

        // Run the JPQL aggregate query defined in PullRequestRepository
        // Each Object[] contains: [0] = author (String), [1] = count (Long)
        List<TrendDto> trends = pullRequestRepository.countPrsByAuthor()
                .stream()
                .map(row -> new TrendDto(
                        (String) row[0],   // author
                        (Long)   row[1]    // prCount
                ))
                .toList();

        log.info("GET /api/trends — returning data for {} authors", trends.size());
        return ResponseEntity.ok(trends);
    }
}
