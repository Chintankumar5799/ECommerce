package com.example.demo.auth.dao;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

public class LoginRequest {
	
	
	    @NotBlank(message = "Email is required")
	    @Email(message = "Invalid email format")
	    private String email;
	    
	    @NotBlank(message = "Password is required")
	    @Size(min = 8, max = 255, message = "Password must be at least 8 characters")
	    private String password;
	    
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	

	    // getters and setters\}
	    

}
