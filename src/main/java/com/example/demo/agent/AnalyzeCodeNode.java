package com.example.demo.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Second node in the review agent graph.
 * Sends the parsed diff to GPT-4o and gets back a raw code analysis.
 * This is where the LLM first gets involved.
 */
@Slf4j
@RequiredArgsConstructor
public class AnalyzeCodeNode implements AsyncNodeAction<ReviewAgentState> {

    // Name used to register this node in the graph
    public static final String NAME = "analyzeCode";

    // Spring AI chat client — used to send prompts to GPT-4o
    private final ChatClient chatClient;

    /**
     * Reads the parsed diff from state, sends it to GPT-4o with a
     * code review prompt, and writes the raw analysis back into state.
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(ReviewAgentState state) {
        try {
            // Get the parsed diff produced by ParseDiffNode
            String parsedDiff = state.parsedDiff().orElseThrow(() ->
                    new IllegalStateException("No parsedDiff found in state"));

            log.info("AnalyzeCodeNode: sending diff to LLM for analysis");

            // Build the prompt and send it to GPT-4o
            String analysis = chatClient.prompt()
                    .user(u -> u.text("""
                            You are an expert code reviewer. Analyze the following code diff and provide:
                            1. A summary of what changed
                            2. Code quality issues (naming, structure, duplication)
                            3. Potential bugs or logic errors
                            4. Performance concerns

                            For each issue found specify:
                            - Which file it is in
                            - What the problem is
                            - How to fix it

                            Here is the diff:

                            {diff}
                            """)
                            .param("diff", parsedDiff))
                    .call()
                    .content();

            log.info("AnalyzeCodeNode: received analysis ({} characters)", analysis.length());

            // Write the LLM analysis back into state for the next node to use
            return CompletableFuture.completedFuture(Map.of("analysis", analysis));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}