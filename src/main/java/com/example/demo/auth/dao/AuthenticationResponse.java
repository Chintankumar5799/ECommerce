package com.example.demo.auth.dao;

public class AuthenticationResponse {
	  private String shortJwt;
	  private String longJwt;
	  
	  

	public AuthenticationResponse() {
		super();
	
	}

	public AuthenticationResponse(String shortJwt) {
		this.shortJwt=shortJwt;
		
	}

	public AuthenticationResponse(String shortJwt, String longJwt) {
		this.shortJwt=shortJwt;
		this.longJwt=longJwt;
	}

	public String getShortJwt() {
		return shortJwt;
	}

	public void setShortJwt(String shortJwt) {
		this.shortJwt = shortJwt;
	}

	public String getLongJwt() {
		return longJwt;
	}

	public void setLongJwt(String longJwt) {
		this.longJwt = longJwt;
	}

	
	
}
