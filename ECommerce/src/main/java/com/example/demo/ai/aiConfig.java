package com.example.demo.ai;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class aiConfig{
	
//	@Bean(name="openAiChatClient")
//	public ChatClient openAiChatModel(OpenAiChatModel openAiChatModel) {
//		return ChatClient.builder(openAiChatModel).build();
//	}
//	
//	@Bean(name="ollamaChatClient")
//	public ChatClient ollamaChatModel(OllamaChatModel ollamaChatModel) {
//		return ChatClient.builder(ollamaChatModel).build();
//	}
	
//	@Bean
//	public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
//		return MessageWindowChatMemory.builder()
//				.chatMemoryRepository(jdbcChatMemoryRepository)
//				.maxMessages(10)
//				.build();
//	}
//	
	
//	Below code is for chatmemory using helper class
	@Bean
	public ChatMemory chatMemory() {
		InMemoryChatMemoryRepository inMemoryChatMemoryRepository=new InMemoryChatMemoryRepository();
		return MessageWindowChatMemory.builder()
				.chatMemoryRepository(inMemoryChatMemoryRepository)
				.maxMessages(10)
				.build();
	}
	
	@Bean
	public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
		
		//For make LLM stateful from stateless to remember old prompt
		MessageChatMemoryAdvisor messageChatMemoryAdvisor=MessageChatMemoryAdvisor.builder(chatMemory).build();
		
		// also can use ,new TokenPrintAdvisor() for token use info
		return builder
				.defaultAdvisors(messageChatMemoryAdvisor,new SimpleLoggerAdvisor(), new SafeGuardAdvisor(List.of("games")))
//				.defaultSystem("You are a helpful coding assistant")
//				.defaultOptions(Ollama.builder()
//				 .model("mistral")
//				 .temperature(0.3)
//				 .macTokens(200)
//				 .build())
                .build();        
				
	}
	
}