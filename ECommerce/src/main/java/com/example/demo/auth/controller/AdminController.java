package com.example.demo.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.dao.RegisterRequest;
import com.example.demo.auth.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AdminController {
	
	// Admin and Seller Details
	private static final Logger log = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private UserService userService;

	@PostMapping(value="/sellerRegister",consumes = "application/json", produces = "application/json")
	public String registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		log.info("Registering new seller: {}", registerRequest.getEmail());
		userService.register(registerRequest,"SELLER");
		return "Your account is pending for Approval!";
	}
	
	

}
