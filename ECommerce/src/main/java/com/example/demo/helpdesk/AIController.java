//package com.example.demo.helpdesk;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/AI/helpdesk")
//public class AIController {
//
//	private final AIService service;
//	
//	public AIController(AIService service) {
//		this.service=service;
//	}
//	
//	@PostMapping
//	public ResponseEntity<String> getResponse(@RequestBody String query){
//		return ResponseEntity.ok(service.getResponseFromAssistsnt(query));
//	}
//}
