package com.ecommerce.demo.ai;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class aiConfig {

        // @Bean(name="openAiChatClient")
        // public ChatClient openAiChatModel(OpenAiChatModel openAiChatModel) {
        // return ChatClient.builder(openAiChatModel).build();
        // }
        //
        // @Bean(name="ollamaChatClient")
        // public ChatClient ollamaChatModel(OllamaChatModel ollamaChatModel) {
        // return ChatClient.builder(ollamaChatModel).build();
        // }

        // @Bean
        // public ChatMemory chatMemory(JdbcChatMemoryRepository
        // jdbcChatMemoryRepository) {
        // return MessageWindowChatMemory.builder()
        // .chatMemoryRepository(jdbcChatMemoryRepository)
        // .maxMessages(10)
        // .build();
        // }
        //

        // Below code is for chatmemory using helper class
        @Bean
        public ChatMemory chatMemory() {
                InMemoryChatMemoryRepository inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();
                return MessageWindowChatMemory.builder()
                                .chatMemoryRepository(inMemoryChatMemoryRepository)
                                .maxMessages(10)
                                .build();
        }

        // -------------------------------------------------------
        // ChatClient (with memory + logging + safeguard)
        // -------------------------------------------------------
        @Bean
        public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {

                // For make LLM stateful from stateless to remember old prompt
                MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                                .build();

                // also can use ,new TokenPrintAdvisor() for token use info
                return builder
                                .defaultAdvisors(messageChatMemoryAdvisor, new SimpleLoggerAdvisor(),
                                                new SafeGuardAdvisor(List.of("games")))
                                // .defaultSystem("You are a helpful coding assistant")
                                // .defaultOptions(Ollama.builder()
                                // .model("mistral")
                                // .temperature(0.3)
                                // .macTokens(200)
                                // .build())
                                .build();
        }

        // -------------------------------------------------------
        // VectorStore — manually wired to MariaDB ONLY.
        // Auto-configuration (MariaDbStoreAutoConfiguration) is excluded
        // in ECommerceApplication.java to prevent it from picking up
        // the @Primary PostgreSQL DataSource by mistake.
        // -------------------------------------------------------
        @Bean
        public VectorStore vectorStore(
                        @Qualifier("mariadbJdbcTemplate") JdbcTemplate mariadbJdbcTemplate,
                        EmbeddingModel embeddingModel) {

                return MariaDBVectorStore.builder(mariadbJdbcTemplate, embeddingModel)
                                .initializeSchema(true)
                                .distanceType(MariaDBVectorStore.MariaDBDistanceType.COSINE)
                                .dimensions(1024)
                                .build();
        }
}