package com.ecommerce.demo.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

//import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpSt)
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
