package com.example.demo.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fourth node in the review agent graph.
 * Takes the general analysis and security findings from previous nodes
 * and asks GPT-4o to combine them into one clean structured review
 * with proper severity labels ready to be posted to GitHub.
 */
@Slf4j
@RequiredArgsConstructor
public class GenerateReviewNode implements AsyncNodeAction<ReviewAgentState> {

    // Name used to register this node in the graph
    public static final String NAME = "generateReview";

    // Spring AI chat client — used to send prompts to GPT-4o
    private final ChatClient chatClient;

    /**
     * Reads analysis and securityFindings from state, sends both to GPT-4o
     * asking it to produce a final structured review, and writes the
     * result back into state as "finalReview".
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(ReviewAgentState state) {
        try {
            // Get the general code analysis from AnalyzeCodeNode
            String analysis = state.analysis().orElseThrow(() ->
                    new IllegalStateException("No analysis found in state"));

            // Get the security findings from SecurityCheckNode
            String securityFindings = state.securityFindings().orElseThrow(() ->
                    new IllegalStateException("No securityFindings found in state"));

            log.info("GenerateReviewNode: combining analysis and security findings into final review");

            // Ask GPT-4o to combine both inputs into one clean structured review
            String finalReview = chatClient.prompt()
                    .user(u -> u.text("""
                            You are a senior engineer writing a pull request review.
                            You have been given two inputs:

                            1. General Code Analysis:
                            {analysis}

                            2. Security Findings:
                            {securityFindings}

                            Combine these into a single clean pull request review using this exact format:

                            ## 🤖 AI Code Review

                            ### Summary
                            (2-3 sentence overview of the changes)

                            ### Issues Found

                            🔴 CRITICAL — (security issues go here)
                            🟡 WARNING  — (bugs and important issues go here)
                            🔵 INFO     — (minor suggestions go here)

                            ### Security
                            (security findings summary)

                            ### Recommendations
                            (top 3 actionable things the author should fix)

                            Keep the tone professional and constructive.
                            If there are no issues at a severity level, omit that level.
                            """)
                            .param("analysis", analysis)
                            .param("securityFindings", securityFindings))
                    .call()
                    .content();

            log.info("GenerateReviewNode: final review generated ({} characters)", finalReview.length());

            // Write the formatted review into state for PostCommentNode to post to GitHub
            return CompletableFuture.completedFuture(Map.of("finalReview", finalReview));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}