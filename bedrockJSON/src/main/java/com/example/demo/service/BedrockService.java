package com.example.demo.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.List;
@Service
public class BedrockService {

    private final BedrockRuntimeClient client;

    public BedrockService(BedrockRuntimeClient client) {
        this.client = client;
    }

    public String generateResponse(String prompt) {

        Message userMessage = Message.builder()
                .role(ConversationRole.USER)
                .content(ContentBlock.fromText(prompt))
                .build();

        InferenceConfiguration inferenceConfiguration = InferenceConfiguration.builder()
                .maxTokens(200)
                .build();

        ConverseRequest request = ConverseRequest.builder()
                .modelId("anthropic.claude-3-haiku-20240307-v1:0")
                .messages(List.of(userMessage))
                .inferenceConfig(inferenceConfiguration)
                .build();

        ConverseResponse response = client.converse(request);

        return response.output()
                .message()
                .content()
                .get(0)
                .text();
    }
}