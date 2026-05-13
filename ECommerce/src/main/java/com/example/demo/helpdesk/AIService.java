//package com.example.demo.helpdesk;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AIService {
//	
//	private final ChatClient chatClient;
//	
//	public AIService(ChatClient chatClient) {
//		this.chatClient=chatClient;
//	}
//
//	public String getResponseFromAssistsnt(String query) {
//		return this.chatClient
//				.prompt()
//				.system("Summarize the response within 200 words")
//				.user(query)
//				.call()
//				.content();
//	}
//}
