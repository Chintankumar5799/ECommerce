package com.example.demo.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

	@GetMapping("/hi")
	public String hi() {
		return "Hello, World!";
	}
	
	  @GetMapping("/hello")
	    public String hello() {
	        return "Hello! Use JWT returned by OAuth2 login for API calls.";
	    }
	
//	@GetMapping("/oAuth")
//	    public String userInfo(@AuthenticationPrincipal OidcUser oidcUser) {
//	        return "Hello " + oidcUser.getFullName() + ", email: " + oidcUser.getEmail();
//    }

}
