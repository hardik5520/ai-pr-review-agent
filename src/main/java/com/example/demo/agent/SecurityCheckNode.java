package com.example.demo.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Third node in the review agent graph.
 * Runs a dedicated security audit on the diff using GPT-4o.
 * Specifically checks for OWASP Top 10 vulnerability patterns.
 * Separate from general analysis to get sharper, more focused security findings.
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityCheckNode implements AsyncNodeAction<ReviewAgentState> {

    // Name used to register this node in the graph
    public static final String NAME = "securityCheck";

    // Spring AI chat client — used to send prompts to GPT-4o
    private final ChatClient chatClient;

    /**
     * Reads the parsed diff from state, sends it to GPT-4o with a
     * security-focused prompt, and writes the findings back into state.
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(ReviewAgentState state) {
        try {
            // Get the parsed diff produced by ParseDiffNode
            String parsedDiff = state.parsedDiff().orElseThrow(() ->
                    new IllegalStateException("No parsedDiff found in state"));

            log.info("SecurityCheckNode: running security audit on diff");

            // Send a security-focused prompt to GPT-4o
            String securityFindings = chatClient.prompt()
                    .user(u -> u.text("""
                            You are a security expert specializing in application security.
                            Analyze the following code diff for security vulnerabilities.

                            Check specifically for OWASP Top 10 issues including:
                            - Injection vulnerabilities (SQL, command, LDAP)
                            - Broken authentication or weak token handling
                            - Sensitive data exposure (passwords, keys, tokens in code)
                            - Security misconfigurations
                            - Use of vulnerable or outdated components
                            - Insecure direct object references
                            - Missing input validation or sanitization
                            - Hardcoded credentials or secrets

                            For each vulnerability found specify:
                            - Severity: CRITICAL, WARNING, or INFO
                            - Which file and line it is in
                            - What the vulnerability is
                            - How to fix it

                            If no security issues are found, respond with "No security vulnerabilities detected."

                            Here is the diff:

                            {diff}
                            """)
                            .param("diff", parsedDiff))
                    .call()
                    .content();

            log.info("SecurityCheckNode: security audit complete ({} characters)", securityFindings.length());

            // Write the security findings back into state for GenerateReviewNode to use
            return CompletableFuture.completedFuture(Map.of("securityFindings", securityFindings));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}