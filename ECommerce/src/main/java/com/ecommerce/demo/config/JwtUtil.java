package com.ecommerce.demo.config;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ecommerce.demo.ECommerceApplication;
import com.ecommerce.demo.auth.dto.AppConstants;
import com.ecommerce.demo.auth.dto.CustomUserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secretKey;

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

	public String generateToken(UserDetails userDetails, Long userId) {
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		log.info("Token generation is initiated......");
		return Jwts.builder()
				.setSubject(userDetails.getUsername())
				.claim("roles", roles)
				.claim("type", "ACCESS") // Added type claim
				.claim("userId", userId)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + AppConstants.SHORT_EXPIRATION))
				.signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}

	public String generateRefreshToken(UserDetails userDetails, Long userId) {
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		log.info("Refresh Token generation is initiated for {} ", userDetails.getUsername());
		return Jwts.builder()
				.setSubject(userDetails.getUsername())
				.claim("roles", roles)
				.claim("type", "REFRESH") // Added type claim
				.claim("userId", userId)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + AppConstants.LONG_EXPIRATION))
				.signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}

	// public String generateToken(String username) {
	// return Jwts.builder()
	// .setSubject(username)
	// .setIssuedAt(new Date())
	// .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
	// .signWith(SignatureAlgorithm.HS256, secretKey)
	// .compact();
	// }

	public boolean validateToken(String token) {

		log.info("Token validation is initiated");
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
				.build()
				.parseClaimsJws(token)
				.getBody();

		String type = claims.get("type", String.class);
		log.info("validate token type" + type);
		return "ACCESS".equals(type) && claims.getExpiration().after(new Date());
	}

	public boolean validateRefreshToken(String token) {
		try {
			log.info("Token validation for refresh token is initiated");
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
					.build()
					.parseClaimsJws(token)
					.getBody();

			String type = claims.get("type", String.class);
			return "REFRESH".equals(type) && claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	// Convert token to Spring Security authentication
	public CustomUserPrincipal getAuthentication(String token) {

		Claims claims = Jwts.parserBuilder()
				.setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
				.build()
				.parseClaimsJws(token)
				.getBody();
		log.info("Authentication of token is initiated");

		Long userId = claims.get("userId", Long.class);
		String username = claims.getSubject();
		List<String> roles = claims.get("roles", List.class);
		log.info("Token contains roles {} ", roles.toArray());

		List<SimpleGrantedAuthority> authorities = roles.stream()
				.map(SimpleGrantedAuthority::new)
				.toList();

		if (username == null)
			return null;

		return new CustomUserPrincipal(username, "", authorities, userId);
		// For demo, we give a default ROLE_USER & it fails for get userId
		// return new UsernamePasswordAuthenticationToken(
		// username,
		// null,
		// authorities
		// // Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
		// );
	}

}
