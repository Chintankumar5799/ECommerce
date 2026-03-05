package com.example.demo.auth.dao;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;

import com.example.demo.auth.entity.Address;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

public class RegisterRequest {


    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;


    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters")
    private String passwordHash;


    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;
    
    @NotNull(message= "Address is required")
    private List<Address> address;
    
    
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public List<Address> getAddress() {
		return address;
	}
	public void setAddress(List<Address> address) {
		this.address = address;
	}
	
	
	// public boolean isEnabled() {
	// 	return enabled;
	// }
	// public void setEnabled(boolean enabled) {
	// 	this.enabled = enabled;
	// }
	
	// public boolean isDeleted() {
	// 	return deleted;
	// }
	// public void setDeleted(boolean deleted) {
	// 	this.deleted = deleted;
	// }
	// public Instant getCreatedAt() {
	// 	return createdAt;
	// }
	// public void setCreatedAt(Instant createdAt) {
	// 	this.createdAt = createdAt;
	// }
	// public Instant getUpdatedAt() {
	// 	return updatedAt;
	// }
	// public void setUpdatedAt(Instant updatedAt) {
	// 	this.updatedAt = updatedAt;
	// }
	
}
