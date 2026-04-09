package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableJpaAuditing //Without this auditing field stay null
@EnableMethodSecurity
public class ECommerceApplication {
	
    private static final Logger logger = LoggerFactory.getLogger(ECommerceApplication.class);


	public static void main(String[] args) {
		
     SpringApplication.run(ECommerceApplication.class, args);
     logger.info("start of logger");
	
	}

}
