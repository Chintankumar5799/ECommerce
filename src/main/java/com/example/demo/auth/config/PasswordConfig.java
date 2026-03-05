package com.example.demo.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
    @Bean  //TO remove Circular dependency issue, Separate from SecurityConfig
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    	
    }

}
