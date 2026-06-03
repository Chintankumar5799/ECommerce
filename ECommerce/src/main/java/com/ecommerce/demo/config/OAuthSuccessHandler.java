package com.ecommerce.demo.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;

import com.ecommerce.demo.auth.entity.User;
import com.ecommerce.demo.auth.repository.UserRepository;
import com.ecommerce.demo.auth.service.UserService;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

	private JwtUtil jwtUtil;
	private UserService userService;
	private UserRepository userRepository;

	@Value("${app.oauth2.redirect-uri}")
	private String redirectUrl;

	private static final Logger log = LoggerFactory.getLogger(OAuthSuccessHandler.class);

	public OAuthSuccessHandler(JwtUtil jwtUtil, UserService userService, UserRepository userRepository) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
		this.userRepository = userRepository;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException {

		log.info("After login using OAuth");
		Object principal = authentication.getPrincipal();

		UserDetails userDetails;

		OAuth2User oauthUser = (OAuth2User) principal;
		String email = oauthUser.getAttribute("email");
		userDetails = userService.processOAuthPostLogin(email);

		User user = userRepository.findByEmail(email);
		Long userId = user.getId();

		String accessToken = jwtUtil.generateToken(userDetails,userId);
		String refreshToken = jwtUtil.generateRefreshToken(userDetails,userId);

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
