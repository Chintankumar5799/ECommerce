package com.example.demo.auth.dto;

public class AuthenticationResponse {
	private String accessToken;
	private String refreshToken;
	private Long userId;

	public AuthenticationResponse() {
		super();
	}

	public AuthenticationResponse(String accessToken) {
		this.accessToken = accessToken;
	}

	public AuthenticationResponse(String accessToken, String refreshToken, Long userId) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.userId = userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
