package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.BedrockService;


@RestController
@RequestMapping("/ai")
public class AIController {

    private final BedrockService service;

    public AIController(BedrockService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public String generate(@RequestBody String prompt) {
        return service.generateResponse(prompt);
    }
}