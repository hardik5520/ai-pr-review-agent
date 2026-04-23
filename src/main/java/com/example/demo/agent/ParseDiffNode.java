package com.example.demo.agent;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * First node in the review agent graph.
 * Takes the raw unified diff from GitHub and breaks it into
 * clean per-file sections so the LLM can process it more effectively.
 */
@Slf4j
public class ParseDiffNode implements AsyncNodeAction<ReviewAgentState> {

    // Name used to register this node in the graph
    public static final String NAME = "parseDiff";

    /**
     * Reads the raw diff from state, parses it into per-file chunks,
     * and writes the result back into state as "parsedDiff".
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(ReviewAgentState state) {
        try {
            // Get the raw diff that was set by the webhook controller before the graph started
            String rawDiff = state.diff().orElseThrow(() ->
                    new IllegalStateException("No diff found in state"));

            log.info("ParseDiffNode: parsing diff of {} characters", rawDiff.length());

            // Split the diff into per-file sections
            // Each file in a unified diff starts with "diff --git"
            String[] fileSections = rawDiff.split("(?=diff --git)");

            // Build a clean readable summary of each file's changes
            StringBuilder parsed = new StringBuilder();
            for (String section : fileSections) {
                if (section.isBlank()) continue;

                // Extract the file name from the diff header line
                // Header format: "diff --git a/src/Foo.java b/src/Foo.java"
                String firstLine = section.lines().findFirst().orElse("");
                String fileName = firstLine.replace("diff --git a/", "").split(" ")[0];

                parsed.append("=== File: ").append(fileName).append(" ===\n");
                parsed.append(section.trim()).append("\n\n");
            }

            log.info("ParseDiffNode: parsed into {} file sections", fileSections.length);

            // Write the parsed diff back into state for the next node to use
            return CompletableFuture.completedFuture(Map.of("parsedDiff", parsed.toString()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}