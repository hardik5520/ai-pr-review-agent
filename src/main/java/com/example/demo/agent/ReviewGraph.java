package com.example.demo.agent;

import com.example.demo.github.GitHubClient;
import com.example.demo.review.PullRequestRepository;
import com.example.demo.review.ReviewCommentRepository;
import com.example.demo.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * Defines and compiles the LangGraph4j agent workflow.
 * Wires all nodes together in order and returns a compiled graph
 * ready to be executed by ReviewService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewGraph {

    // Spring AI chat client — shared across all LLM nodes
    private final ChatClient chatClient;

    // GitHub client — used by PostCommentNode to post the review
    private final GitHubClient gitHubClient;

    // Repositories — used by PostCommentNode to save results to DB
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final PullRequestRepository pullRequestRepository;

    /**
     * Builds and compiles the full review agent graph.
     * Defines nodes and edges — edges determine the execution order.
     * Returns a CompiledGraph ready to be run with an initial state.
     */
    public CompiledGraph<ReviewAgentState> build() throws Exception {

        // Create instances of each node, injecting their dependencies
        ParseDiffNode parseDiffNode = new ParseDiffNode();
        AnalyzeCodeNode analyzeCodeNode = new AnalyzeCodeNode(chatClient);
        SecurityCheckNode securityCheckNode = new SecurityCheckNode(chatClient);
        GenerateReviewNode generateReviewNode = new GenerateReviewNode(chatClient);
        PostCommentNode postCommentNode = new PostCommentNode(
                gitHubClient,
                reviewRepository,
                reviewCommentRepository,
                pullRequestRepository
        );

        // Build the graph — define nodes and the edges connecting them
        return new StateGraph<>(ReviewAgentState::new)

                // Register all nodes with their names
                .addNode(ParseDiffNode.NAME, parseDiffNode)
                .addNode(AnalyzeCodeNode.NAME, analyzeCodeNode)
                .addNode(SecurityCheckNode.NAME, securityCheckNode)
                .addNode(GenerateReviewNode.NAME, generateReviewNode)
                .addNode(PostCommentNode.NAME, postCommentNode)

                // Define edges — these determine execution order
                // START → parseDiff → analyzeCode → securityCheck → generateReview → postComment → END
                .addEdge(START, ParseDiffNode.NAME)
                .addEdge(ParseDiffNode.NAME, AnalyzeCodeNode.NAME)
                .addEdge(AnalyzeCodeNode.NAME, SecurityCheckNode.NAME)
                .addEdge(SecurityCheckNode.NAME, GenerateReviewNode.NAME)
                .addEdge(GenerateReviewNode.NAME, PostCommentNode.NAME)
                .addEdge(PostCommentNode.NAME, END)

                // Compile the graph — validates the structure and prepares it for execution
                .compile();
    }
}