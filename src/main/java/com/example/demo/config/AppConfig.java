package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-wide Spring configuration.
 * Defines beans that need to be manually created and registered.
 */
// Marks this class as a source of Spring bean definitions
@Configuration
public class AppConfig {

    /**
     * Creates a ChatClient bean backed by OpenAI's GPT-4o model.
     * Spring AI auto-configures the OpenAiChatModel from application.yml credentials.
     * We wrap it in a ChatClient so all nodes can use the fluent prompt API.
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        // Build a ChatClient using the auto-configured OpenAI model
        return ChatClient.builder(openAiChatModel).build();
    }
}