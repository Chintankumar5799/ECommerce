package com.example.demo.auth.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.auth.controller.AuthController;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
   
    
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
    	
    	
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
        	log.info("Authorization header found");
            String token = header.substring(7);
            
            try {
            if (jwtUtil.validateToken(token)) {
            	
                UsernamePasswordAuthenticationToken auth =
                        jwtUtil.getAuthentication(token); // set roles, username, etc.
                SecurityContextHolder.getContext().setAuthentication(auth);
//                logger.info("token is valid for {}",token.get
                log.info("Token is valid");
                
                
            }} catch (ExpiredJwtException ex) {
            	log.info("Token is Expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Token Expired\"}");
                return;
            } catch (SignatureException | MalformedJwtException ex) {
            	log.info("Token validation failed");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid Token\"}");
                return;
            }
                
                
            }
        
        chain.doFilter(request, response);
    }
	
}

