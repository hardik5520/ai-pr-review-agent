package com.example.demo.review;

import com.example.demo.agent.ReviewAgentState;
import com.example.demo.agent.ReviewGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service that orchestrates the AI review workflow.
 * Builds the agent graph, feeds it the initial state,
 * and runs it to completion.
 * Called by WebhookController when a PR is opened or updated.
 */
// Marks this as a Spring service bean — sits between the controller and the agent graph
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    // The graph builder — wires all agent nodes together
    private final ReviewGraph reviewGraph;

    /**
     * Runs the full review agent graph for a given PR.
     * Feeds the diff, repo name, and PR number as initial state,
     * then executes each node in order until the graph completes.
     *
     * @param repoName full repository name e.g. "hardik5520/pr-review-test"
     * @param prNumber PR number within the repository
     * @param diff     raw unified diff text fetched from GitHub
     */
    public void runReview(String repoName, int prNumber, String diff) {
        try {
            log.info("ReviewService: starting review for PR #{} in {}", prNumber, repoName);

            // Build the compiled graph — connects all nodes with edges
            CompiledGraph<ReviewAgentState> graph = reviewGraph.build();

            // Set the initial state — this is what the first node (ParseDiffNode) reads from
            Map<String, Object> initialState = Map.of(
                    "repoName", repoName,
                    "prNumber", prNumber,
                    "diff", diff
            );

            // Execute the graph — runs every node in order until END is reached
            graph.stream(initialState)
                    .forEach(step -> log.info("ReviewService: completed step → {}", step.node()));

            log.info("ReviewService: review complete for PR #{}", prNumber);

        } catch (Exception e) {
            log.error("ReviewService: review failed for PR #{} — {}", prNumber, e.getMessage(), e);
        }
    }
}