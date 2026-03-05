package com.example.demo.auth.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.auth.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler{

	private JwtUtil jwtUtil;
	private UserService userService;
	
	@Value("${app.oauth2.redirect-uri}")
	private String redirectUrl;
	
	public OAuthSuccessHandler(JwtUtil jwtUtil, UserService userService) {
		this.jwtUtil=jwtUtil;
		this.userService=userService;
	}

	 @Override
	    public void onAuthenticationSuccess(
	            HttpServletRequest request,
	            HttpServletResponse response,
	            Authentication authentication) throws IOException {
	    	

		 	Object principal = authentication.getPrincipal();

		    UserDetails userDetails;
		    
		    OAuth2User oauthUser = (OAuth2User) principal;
	        String email = oauthUser.getAttribute("email");

	        userDetails = userService.processOAuthPostLogin(email);	   
	        String accessToken = jwtUtil.generateToken(userDetails);
	        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
	        
            // Clears the session immediately... (rest same)
	        SecurityContextHolder.clearContext();
	        HttpSession session = request.getSession(false);
	        if (session != null) {
	            session.invalidate();
	        }
	        
	        response.setContentType("application/json");
	        response.sendRedirect(redirectUrl + accessToken + "&refreshToken=" + refreshToken);

	    }


}
