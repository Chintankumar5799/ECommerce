package com.example.demo.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

import com.example.demo.auth.dao.AppConstants;
import com.example.demo.auth.service.UserService;

import jakarta.servlet.Filter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserService userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userService = userService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	
        http
            .csrf(csrf -> csrf.disable()) //  Disable CSRF for REST APIs
            .cors(Customizer.withDefaults()) //  Enable CORS
            //.sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            
            // Handle exceptions: Return 401 for XMLHttpRequests or API calls instead of redirecting to login
            .exceptionHandling(exception -> exception
                    .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> request.getRequestURI().startsWith("/api/")
                    )
                )

            .authorizeHttpRequests(auth -> auth
            		.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register","/api/auth/refresh","/api/auth/sellerRegister").permitAll()
                .requestMatchers("/oauth-success").permitAll()
                .requestMatchers("/api/user/hi","/api/category/**","/api/product/**","/api/cart/**","/api/order/**").hasAnyRole(AppConstants.ROLE_BUYER,AppConstants.ROLE_SELLER)
//                .requestMatchers("/api/product/newProduct").hasRole(AppConstants.ROLE_SELLER)
                //                .requestMatchers("/api/seller/").hasRole(AppConstants.ROLE_SELLER)
                .anyRequest().authenticated()
            
                )
//            .oauth2Login(oauth2 -> oauth2
////                    .defaultSuccessUrl("/home", true)
//                    .successHandler(oAuthSuccessHandler)
//                )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    


    @Bean
    public JwtUtil jwtUtility() {
        return new JwtUtil(); // your service with validateToken and getAuthentication
    }

	@Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }    
    
    // Not required in latest Spring Security but if we call it then need to define
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
	    return configuration.getAuthenticationManager();
	}
	
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        // Allow likely frontend origins (localhost:3000 for React, etc.)
        configuration.setAllowedOrigins(java.util.List.of(
            "http://localhost:3000", 
            "http://localhost:4200", 
            "http://localhost:5173", 
            "http://localhost:5174", 
            "http://localhost:8081"
        ));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    



}


// Extra Code 
//  Hard coded login
//  @Bean
//  public UserDetailsService userDetailsService() {
//      UserDetails user = User.builder()
//              .username("user")
//              .password("$2a$12$sgmrTlGq3TK5dAzrq9Yg/OvLPE92sAhPZcpLNupJnIp81EGgveLvy")
//              .build();
//      return new InMemoryUserDetailsManager(user);
//  }


//       //Form login is without JWT, if JWT then we can put filters as above 
//            .formLogin(form -> form
//                    .passwordParameter("password")
//                    .successHandler(jwtSuccessHandler)
//                    
//                );
//      //Or write below FormLogin
//            .formLogin(form -> form
//            	.loginProcessingUrl("/login")
//                .defaultSuccessUrl("/hi", true)
//            );
            

//             .logout(logout -> logout
//                .logoutUrl("/logout") // Specifies the logout URL (default is "/logout")
//                .logoutSuccessUrl("/login?logout") // URL to redirect to after successful logout
//                .invalidateHttpSession(true) // Invalidates the HTTP session (default is true)
//                .deleteCookies("JSESSIONID") // Deletes specified cookies on logout
//                .permitAll() // Allows access to the logout URL for all
//            )
        
//       //If without JWT then we can use below code to manage sessions
//             .sessionManagement(session -> session
//            .maximumSessions(1)
//            .sessionRegistry(sessionRegistry())
//        );