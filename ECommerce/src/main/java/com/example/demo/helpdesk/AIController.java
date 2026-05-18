package com.example.demo.helpdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/AI/helpdesk")
public class AIController {

    private final AIService service;

    private final static Logger log=LoggerFactory.getLogger(AIController.class);
    
    public AIController(AIService service) {
        this.service = service;
    }

    @PostMapping("/call")
    public ResponseEntity<String> getResponse(@RequestBody String query, @RequestHeader("ConversationId") String conversationId) {
       log.info("Chat call with "+query+" "+conversationId);
    	return ResponseEntity.ok(service.getResponseFromAssistsnt(query,conversationId));
    }
    
    
    @PostMapping("/stream")
    public Flux<String> streamResponse(@RequestBody String query, @RequestHeader("ConversationId") String conversationId) {
    	 log.info("Chat stream with "+query+" "+conversationId);
    	return this.service.streamResponseFromAssistant(query, conversationId);
    }
}



















