package com.example.demo.auth.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.demo.auth.dao.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;



@Entity
@Table(name="users",
		uniqueConstraints= {
				@UniqueConstraint(columnNames="email"),
				@UniqueConstraint(columnNames="mobile_number")
		})
public class User extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@Column(nullable = false, length=150)
	@Email
	private String email;
	
	@OneToMany(mappedBy="user",cascade=CascadeType.ALL, orphanRemoval=true)
	private List<Address> address;
	
	@Column(name="password_hash",nullable = false, length=255)
	@JsonIgnore
	private String passwordHash;
	
	@Column(name="mobile_number",nullable = false, length=15)
	private String mobileNumber;
	
	@Column(nullable = false)
	private boolean enabled;
	
	@Column(nullable = false)
	private boolean emailVerified=false;
	
	@Column(nullable = false)
	private boolean phoneVerified=false;
	
	@Column(name="deleted",nullable = false)
	private boolean deleted = false;
	
	@ManyToMany
	@JoinTable(name="users_roles",
	joinColumns=@JoinColumn(name="user_id"),
	inverseJoinColumns=@JoinColumn(name="role_id"))
	private Set<Role> roles=new HashSet<>();
	
	@Column(name="approved",nullable=false)
	private boolean approved=false;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status",nullable = false)
	private UserStatus status=UserStatus.PENDING;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return passwordHash;
	}

	public void setPassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	

	public List<Address> getAddress() {
		return address;
	}

	public void setAddress(List<Address> address) {
		this.address = address;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public boolean isPhoneVerified() {
		return phoneVerified;
	}

	public void setPhoneVerified(boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

	public boolean deleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

//	public String getPasswordHash() {
//		return passwordHash;
//	}
//
//	public void setPasswordHash(String passwordHash) {
//		this.passwordHash = passwordHash;
//	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public boolean isDeleted() {
		return deleted;
	}
	
	
	

}
