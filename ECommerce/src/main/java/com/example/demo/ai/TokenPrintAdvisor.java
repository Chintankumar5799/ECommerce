package com.example.demo.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;

import com.example.demo.auth.controller.AdminController;

import reactor.core.publisher.Flux;

public class TokenPrintAdvisor implements CallAdvisor, StreamAdvisor {
	
	private static final Logger log = LoggerFactory.getLogger(AdminController.class);
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getName();
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		
		Flux<ChatClientResponse> chatClientResponseFlux=streamAdvisorChain.nextStream(chatClientRequest);
		return chatClientResponseFlux;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		log.info("Advisor is called");
		log.info("Request: "+ chatClientRequest.prompt().getContents());
		
		ChatClientResponse chatClientResponse=callAdvisorChain.nextCall(chatClientRequest);
		
		log.info("Advisor is received");
		log.info("Response: "+chatClientResponse.chatResponse().getMetadata().getUsage().getTotalTokens());
		return chatClientResponse;
	}

	
}
