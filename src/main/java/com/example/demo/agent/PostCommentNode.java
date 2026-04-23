package com.example.demo.agent;

import com.example.demo.github.GitHubClient;
import com.example.demo.review.PullRequestEntity;
import com.example.demo.review.PullRequestRepository;
import com.example.demo.review.ReviewCommentEntity;
import com.example.demo.review.ReviewCommentRepository;
import com.example.demo.review.ReviewEntity;
import com.example.demo.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Final node in the review agent graph.
 * Posts the generated review as a comment on the GitHub PR
 * and saves the review and its comments to the database.
 */
@Slf4j
@RequiredArgsConstructor
public class PostCommentNode implements AsyncNodeAction<ReviewAgentState> {

    // Name used to register this node in the graph
    public static final String NAME = "postComment";

    // GitHub client — used to post the review comment to the PR
    private final GitHubClient gitHubClient;

    // Repositories — used to save the review and comments to the database
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final PullRequestRepository pullRequestRepository;

    /**
     * Reads the final review from state, posts it to GitHub as a PR comment,
     * saves the review to the database, and parses individual comments
     * to save as review_comments rows.
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(ReviewAgentState state) {
        try {
            // Get all required values from state
            String finalReview = state.finalReview().orElseThrow(() ->
                    new IllegalStateException("No finalReview found in state"));

            String repoName = state.repoName().orElseThrow(() ->
                    new IllegalStateException("No repoName found in state"));

            int prNumber = state.prNumber().orElseThrow(() ->
                    new IllegalStateException("No prNumber found in state"));

            log.info("PostCommentNode: posting review to PR #{} in {}", prNumber, repoName);

            // Determine overall severity based on what the review contains
            String overallSeverity = "INFO";
            if (finalReview.contains("🔴 CRITICAL")) {
                overallSeverity = "CRITICAL";
            } else if (finalReview.contains("🟡 WARNING")) {
                overallSeverity = "WARNING";
            }

            // Find the most recent pull request record in the database
            PullRequestEntity pr = pullRequestRepository
                    .findFirstByRepoNameAndPrNumberOrderByCreatedAtDesc(repoName, prNumber)
                    .orElseThrow(() -> new IllegalStateException("PR not found in database"));

            // Save the review to the database
            ReviewEntity review = ReviewEntity.builder()
                    .pullRequest(pr)
                    .summary(finalReview)
                    .overallSeverity(overallSeverity)
                    .modelUsed("gpt-4o")
                    .createdAt(LocalDateTime.now())
                    .build();

            reviewRepository.save(review);
            log.info("PostCommentNode: saved review to database with severity {}", overallSeverity);

            // Parse the review text and save individual comment lines to the database
            // Each line starting with 🔴, 🟡, or 🔵 is treated as a separate comment
            for (String line : finalReview.lines().toList()) {
                if (line.startsWith("🔴") || line.startsWith("🟡") || line.startsWith("🔵")) {

                    // Determine the severity of this specific line
                    String severity = line.startsWith("🔴") ? "CRITICAL"
                            : line.startsWith("🟡") ? "WARNING"
                            : "INFO";

                    ReviewCommentEntity comment = ReviewCommentEntity.builder()
                            .review(review)
                            .filePath("general")
                            .lineNumber(0)
                            .severity(severity)
                            .commentText(line)
                            .createdAt(LocalDateTime.now())
                            .build();

                    reviewCommentRepository.save(comment);
                }
            }

            // Post the final review as a comment on the GitHub PR
            gitHubClient.postComment(repoName, prNumber, finalReview);
            log.info("PostCommentNode: posted review comment to GitHub PR #{}", prNumber);

            // Return empty map — this is the last node, nothing more to add to state
            return CompletableFuture.completedFuture(Map.of());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}