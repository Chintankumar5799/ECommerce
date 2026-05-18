package com.example.demo.helpdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.category.service.SubCategoryService;

@Configuration
public class AIConfig {
	
	private final static Logger log=LoggerFactory.getLogger(AIConfig.class);

//	public JdbcChatMemoryRepository jdbcChatMemoryRepository() {
//		return JdbcChatMemoryRepository.builder()
//				.jdbcTemplate()
//				.jdbcTemplate()
//				.build();
//	}
	
	
	@Bean
	public ChatClient chatClint(ChatClient.Builder builder,JdbcChatMemoryRepository jdbcChatMemoryRepository) {
//			ChatMemory chatMemory) {

		log.info("Inside chatClient method");
		var chatMemory=MessageWindowChatMemory.builder()
			.chatMemoryRepository(jdbcChatMemoryRepository)
			.maxMessages(15)
			.build();
		
		log.info("JDBC Repo is set for Chat history");
		
		return builder
		.defaultSystem("Summarize the response within 400 words.")
		.defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build())
		.build();
		
	}
}
