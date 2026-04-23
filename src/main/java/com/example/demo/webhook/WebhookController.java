package com.example.demo.webhook;

import com.example.demo.github.GitHubClient;
import com.example.demo.review.PullRequestEntity;
import com.example.demo.review.PullRequestRepository;
import com.example.demo.review.ReviewService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// Marks this class as a REST controller — all methods return response bodies directly (no view rendering)
@RestController
// Maps all endpoints in this controller under the "/webhook" base path
@RequestMapping("/webhook")
// Lombok: generates a constructor that injects all final fields, satisfying Spring's dependency injection
@RequiredArgsConstructor
// Lombok: injects a logger (log) so we can write log.info(...) and log.error(...) throughout the class
@Slf4j
/**
 * Handles incoming webhook events from GitHub.
 * Receives pull request events, verifies their authenticity, saves PR details to the database,
 * fetches the PR diff, and triggers the AI review agent.
 */
public class WebhookController {

    private final WebhookSignatureValidator signatureValidator;
    private final PullRequestRepository pullRequestRepository;
    private final ObjectMapper objectMapper;
    private final GitHubClient gitHubClient;

    // ReviewService runs the full LangGraph4j agent workflow
    private final ReviewService reviewService;

    /**
     * Receives a GitHub webhook POST request and processes pull request events.
     * Only acts on "opened" and "synchronize" actions.
     * Saves the PR to the database, fetches its diff, and triggers the AI review agent.
     *
     * @param signature the HMAC-SHA256 signature sent by GitHub in the X-Hub-Signature-256 header
     * @param payload   the raw JSON body of the webhook event
     */
    // Handles POST requests to "/webhook/github"
    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            // Reads the GitHub signature from the request header for security validation
            @RequestHeader("X-Hub-Signature-256") String signature,
            // Reads the raw request body as bytes so the original bytes can be used for signature verification
            @RequestBody byte[] payload
    ) throws Exception {

        // Verify the request actually came from GitHub using the shared secret
        signatureValidator.validate(payload, signature);

        // Parse the raw JSON payload into a traversable tree structure
        JsonNode root = objectMapper.readTree(payload);

        // Extract the action field to determine what type of PR event occurred
        String action = root.path("action").asText();

        log.info("Received GitHub webhook. Action: {}", action);

        // Only process events where a PR is newly opened or has new commits pushed to it
        if (action.equals("opened") || action.equals("synchronize")) {
            JsonNode pr = root.path("pull_request");

            // Extract the full repository name (format: "owner/repo")
            String repoName = root.path("repository").path("full_name").asText();

            // Extract the PR number within the repository
            int prNumber = pr.path("number").asInt();

            // Build a new PullRequestEntity record from the webhook data
            PullRequestEntity entity = PullRequestEntity.builder()
                    .repoName(repoName)
                    .prNumber(prNumber)
                    // Extract the GitHub login of the user who opened the PR
                    .author(pr.path("user").path("login").asText())
                    .title(pr.path("title").asText())
                    // Mark as PENDING since the review has not started yet
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            // Persist the pull request record to the database
            pullRequestRepository.save(entity);
            log.info("Saved PR #{} from repo {}", entity.getPrNumber(), entity.getRepoName());

            // Fetch the raw code diff from GitHub for this PR
            String diff = gitHubClient.fetchDiff(repoName, prNumber);
            log.info("Fetched diff ({} characters)", diff.length());

            // Hand off to ReviewService — runs the full AI agent graph asynchronously
            // ParseDiff → AnalyzeCode → SecurityCheck → GenerateReview → PostComment
            reviewService.runReview(repoName, prNumber, diff);
        }

        // Respond with 200 OK so GitHub knows the webhook was received successfully
        return ResponseEntity.ok("ok");
    }
}