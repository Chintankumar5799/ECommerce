package com.example.demo.auth.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.config.JwtUtil;
import com.example.demo.auth.config.SecurityConfig;
import com.example.demo.auth.dao.AuthenticationResponse;
import com.example.demo.auth.dao.LoginRequest;
import com.example.demo.auth.dao.RegisterRequest;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private com.example.demo.auth.repository.UserRepository userRepository;
	
	@Autowired
    private JwtUtil jwtUtil;
	
	@Autowired
	private SecurityConfig securityConfig;
	
	private final AuthenticationManager authenticationManager;
	
	 private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	
	public AuthController(AuthenticationManager authenticationManager) {
		this.authenticationManager=authenticationManager;
	}
	
	
	
	@PostMapping(value="/register",consumes = "application/json", produces = "application/json")
	public String registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		userService.register(registerRequest,"BUYER");
		return "User register Successfully";
	}
	
	 @PostMapping("/login")
	 public ResponseEntity<?> login(@RequestBody LoginRequest request) {
	        try {
	            
	            // Authenticate user
	            Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
	            );

	            UserDetails userDetails=userService.loadUserByUsername(request.getEmail());
	            
	            log.info("User just logged in successfully with email id {} ", request.getEmail());
	           
	            
	            // Generate JWT
	            String  accessToken= jwtUtil.generateToken(userDetails);
	            String refreshToken= jwtUtil.generateRefreshToken(userDetails);
	      

	            User user = userRepository.findByEmail(request.getEmail());
	            if (user == null) {
	                 System.out.println("ERROR: User found by service but not by repository in controller!");
	                 return ResponseEntity.status(500).body("User not found in repository");
	            }
	            System.out.println("DEBUG: User ID: " + user.getId());

	            java.util.Map<String, Object> response = new java.util.HashMap<>();
	            response.put("accessToken", accessToken);
	            response.put("refreshToken", refreshToken);
	            response.put("userId", user.getId());
	            
	            return ResponseEntity.ok(response);
	        } catch (org.springframework.security.authentication.BadCredentialsException e) {
	        	log.warn("User just logged in with bad credential with email id {} ", request.getEmail());
	            return ResponseEntity.status(401).body("Invalid email or password");
	        } catch (Exception e) {
	        	log.error("Authentication error with email id {} ", request.getEmail());
	            e.printStackTrace();
	            return ResponseEntity.status(500).body("Authentication error: " + e.getMessage());
	        }
	    }
	

	  @PostMapping("/refresh")
	  public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
		  String refreshToken = request.get("refreshToken");
	      
		  if (jwtUtil.validateRefreshToken(refreshToken)) {
			  String username = jwtUtil.getAuthentication(refreshToken).getName();
			  UserDetails userDetails = userService.loadUserByUsername(username);
			  log.info("Refresh token request raised by {} ....",userDetails.getUsername());
			  
			  String newAccessToken = jwtUtil.generateToken(userDetails);
			  return ResponseEntity.ok(
					  Map.of("accessToken", newAccessToken)
					  );
		  }
		  
//		  log.warn("Refresh token request cancelled for ....",request);
		  return ResponseEntity.status(403).body("Invalid or expired refresh token");
	  }


 

// verifying AuthController content via read or just trusting the previous successful tool call.
// The tool output 607 showed the diff applied correctly.

// I will now clean up the commented out code in AuthController to make it look professional as requested.

}
