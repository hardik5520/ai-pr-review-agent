package com.example.demo.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Registers this class as a Spring-managed component so it can be injected into other beans
@Component
// Lombok: injects a logger (log) so we can write log.info(...) and
// log.error(...) throughout the class
@Slf4j
/**
 * HTTP client for communicating with the GitHub REST API.
 * Handles fetching pull request diffs and posting review comments on behalf of
 * the configured GitHub token.
 */
public class GitHubClient {

    // Preconfigured HTTP client with the GitHub base URL and required auth headers
    // set once at startup
    private final RestClient restClient;

    /**
     * Builds the RestClient with the GitHub API base URL and authentication
     * headers.
     * The token is injected from the "github.token" property in application
     * configuration.
     */
    public GitHubClient(@Value("${github.token}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                // Authenticate all requests using the personal access token or app token
                .defaultHeader("Authorization", "Bearer " + token)
                // Tell GitHub we expect JSON responses in its standard format
                .defaultHeader("Accept", "application/vnd.github+json")
                // Pin the API version to ensure consistent behavior over time
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    /**
     * Fetches the unified diff of all file changes in the given pull request.
     * Returns the diff as a plain string in the standard patch format.
     *
     * @param repoFullName full repository name in "owner/repo" format
     * @param prNumber     the pull request number
     * @return the raw diff text of the pull request
     */
    public String fetchDiff(String repoFullName, int prNumber) {
        log.info("Fetching diff for PR #{} in {}", prNumber, repoFullName);

        // Build URI as a string to avoid Spring URL-encoding the slash in repoFullName
        return restClient.get()
                .uri("/repos/" + repoFullName + "/pulls/" + prNumber)
                .header("Accept", "application/vnd.github.diff")
                .retrieve()
                .body(String.class);
    }

    /**
     * Posts a comment on the given pull request's issue thread.
     * Used to notify contributors of review status or results.
     *
     * @param repoFullName full repository name in "owner/repo" format
     * @param prNumber     the pull request number
     * @param comment      the text of the comment to post
     */
    public void postComment(String repoFullName, int prNumber, String comment) {
    log.info("Posting comment to PR #{} in {}", prNumber, repoFullName);

    // Build URI as a string to avoid Spring URL-encoding the slash in repoFullName
    restClient.post()
            .uri("/repos/" + repoFullName + "/issues/" + prNumber + "/comments")
            .body(new CommentRequest(comment))
            .retrieve()
            .toBodilessEntity();
    }

    // Simple wrapper record to serialize the comment body as JSON: { "body": "..."
    // }
    record CommentRequest(String body) {
    }
}
