package com.example.demo.helpdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
// import org.springframework.retry.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AIService {

    private final ChatClient chatClient;
    private final TicketDatabaseTool ticketDatabaseTool;
    private final EmailTool emailTool;

    private final static Logger log = LoggerFactory.getLogger(AIService.class);

    public AIService(ChatClient chatClient, TicketDatabaseTool ticketDatabaseTool, EmailTool emailTool) {
        this.chatClient = chatClient;
        this.ticketDatabaseTool = ticketDatabaseTool;
        this.emailTool = emailTool;
    }

    @Value("classpath:/helpdesk-system.st")
    private Resource systemPromptResource;

    @CircuitBreaker(name = "ollamaService", fallbackMethod = "fallbackResponse")
    public String getResponseFromAssistsnt(String query, String conversationId) {

        log.info("call chatclient from service class");
        return this.chatClient
                .prompt()
                // .system("Summarize the response within 200 words")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))

                .tools(ticketDatabaseTool, emailTool)
                .system(systemPromptResource)
                .user(query)
                .call()
                .content();
    }

    // Ollama response failed
    private String fallbackResponse(String query, String conversationId, Throwable t) {
        log.info("Ollama response failed");
        return "Ollama response failed";
    }

    public Flux<String> streamResponseFromAssistant(String query, String conversationId) {
        log.info("stream chatclient from service class");
        return this.chatClient
                .prompt()
                // .system("Summarize the response within 200 words")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))

                .tools(ticketDatabaseTool, emailTool)
                .system(systemPromptResource)
                .user(query)
                .stream()
                .content();
    }
}
