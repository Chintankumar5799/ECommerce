package com.example.demo.auth.entity;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

//This is not Entity nor DTO class its for spring security
//to check role before give details for dashboard

public class CustomUserDetails implements UserDetails {
	private Long id;
    private String email;
    private Set<Role> role;
    
    
	public CustomUserDetails(Long id, String email, Set<Role> roles) {
		// TODO Auto-generated constructor stub
	}

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

	public Set<Role> getRole() {
		return role;
	}

	public void setRole(Set<Role> role) {
		this.role = role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (role == null) return List.of();
		return role.stream()
				.map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName()))
				.toList();
	}

	@Override
	public @Nullable String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

}
