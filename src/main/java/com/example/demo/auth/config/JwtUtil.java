package com.example.demo.auth.config;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.example.demo.ECommerceApplication;
import com.example.demo.auth.dao.AppConstants;

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
	

    public String generateToken(UserDetails userDetails) {
		   List<String> roles = userDetails.getAuthorities().stream()
		            .map(GrantedAuthority::getAuthority)
		            .toList();
		   
	       
	        return Jwts.builder()
	                .setSubject(userDetails.getUsername())
	                .claim("roles", roles)
	                .claim("type", "ACCESS") // Added type claim
	                .setIssuedAt(new Date())
	                .setExpiration(new Date(System.currentTimeMillis() + AppConstants.SHORT_EXPIRATION))
	                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
	                .compact();
	    }
	   
	   public String generateRefreshToken(UserDetails userDetails) {
		   List<String> roles = userDetails.getAuthorities().stream()
		            .map(GrantedAuthority::getAuthority)
		            .toList();
		   
	        return Jwts.builder()
	                .setSubject(userDetails.getUsername())
	                .claim("roles", roles)
	                .claim("type", "REFRESH") // Added type claim
	                .setIssuedAt(new Date())
	                .setExpiration(new Date(System.currentTimeMillis() + AppConstants.LONG_EXPIRATION))
	                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
	                .compact();
	    }
	   
//	   public String generateToken(String username) {
//		    return Jwts.builder()
//		               .setSubject(username)
//		               .setIssuedAt(new Date())
//		               .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
//		               .signWith(SignatureAlgorithm.HS256, secretKey)
//		               .compact();
//		}

	   public boolean validateToken(String token) {
		   System.out.println("validate token"+token);
	            Claims claims = Jwts.parserBuilder()
	                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
	                    .build()
	                    .parseClaimsJws(token)
	                    .getBody();
	            
	            String type = claims.get("type", String.class);
	            System.out.println("validate token type"+type);
	            return "ACCESS".equals(type) && claims.getExpiration().after(new Date());
	    }
	   
	   public boolean validateRefreshToken(String token) {
	        try {
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
	    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
	    	  System.out.println("get Authentication ");
	        Claims claims = Jwts.parserBuilder()
	                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
	                .build()
	                .parseClaimsJws(token)
	                .getBody();
	        
	       
	        String username = claims.getSubject();
	        List<String> roles = claims.get("roles", List.class);
	        System.out.println("get authentication roles");

	        List<SimpleGrantedAuthority> authorities = roles.stream()
	                .map(SimpleGrantedAuthority::new)
	                .toList();
	        
	        if (username == null) return null;

	        // For demo, we give a default ROLE_USER
	        return new UsernamePasswordAuthenticationToken(
	                username,
	                null,
	                authorities
//	                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
	        );
	    }
	
}
