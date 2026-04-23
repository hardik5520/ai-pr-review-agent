package com.example.demo.agent;

import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;
import java.util.Optional;

/**
 * Shared state object passed between every node in the review agent graph.
 * Each node reads what it needs from this state and writes its output back into it.
 * Think of it as a whiteboard that gets passed down the assembly line.
 */
public class ReviewAgentState extends AgentState {

    // Constructor required by LangGraph4j — initialises state with a data map
    public ReviewAgentState(Map<String, Object> initData) {
        super(initData);
    }

    // --- Inputs set before the graph starts ---

    /** Full repository name e.g. "hardik5520/pr-review-test" */
    public Optional<String> repoName() {
        return value("repoName");
    }

    /** PR number within the repository */
    public Optional<Integer> prNumber() {
        return value("prNumber");
    }

    /** Raw unified diff text fetched from GitHub */
    public Optional<String> diff() {
        return value("diff");
    }

    // --- Set by ParseDiffNode ---

    /** Diff split into per-file chunks for easier LLM processing */
    public Optional<String> parsedDiff() {
        return value("parsedDiff");
    }

    // --- Set by AnalyzeCodeNode ---

    /** Raw analysis text returned by GPT-4o */
    public Optional<String> analysis() {
        return value("analysis");
    }

    // --- Set by SecurityCheckNode ---

    /** Security-specific findings from the OWASP check */
    public Optional<String> securityFindings() {
        return value("securityFindings");
    }

    // --- Set by GenerateReviewNode ---

    /** Final formatted review comment ready to be posted to GitHub */
    public Optional<String> finalReview() {
        return value("finalReview");
    }
}