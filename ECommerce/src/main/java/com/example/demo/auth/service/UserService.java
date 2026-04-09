package com.example.demo.auth.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import com.example.demo.ECommerceApplication;
import com.example.demo.auth.controller.AdminController;
import com.example.demo.auth.dao.RegisterRequest;
import com.example.demo.auth.dao.UserStatus;
import com.example.demo.auth.entity.Address;
import com.example.demo.auth.entity.Role;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.exception.UserAlreadyExistsException;
import com.example.demo.auth.repository.RoleRepository;
import com.example.demo.auth.repository.UserRepository;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class UserService implements UserDetailsService  {
  
//    private final ECommerceApplication ECommerceApplication;
	
	 private final UserRepository userRepository;
	 private final PasswordEncoder passwordEncoder;
	 private final RoleRepository roleRepository;
	 
	 private static final Logger log = LoggerFactory.getLogger(UserService.class);

	    public UserService(UserRepository userRepository,RoleRepository roleRepository ,PasswordEncoder passwordEncoder) {
	        this.userRepository = userRepository;
	        this.passwordEncoder=passwordEncoder;
	        this.roleRepository = roleRepository;
	    }

	    @org.springframework.transaction.annotation.Transactional
	    public void register(RegisterRequest request,String rollName) {
	    	if (userRepository.findByEmail(request.getEmail()) != null) {
	    		log.warn("User with {} is already Exists",request.getEmail());
	    		throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
	    	}
	    	
	    	
	    	Role customerRole=roleRepository.findByRoleName(rollName);
	    	
	    	log.info("Customer {} with mobile number {} register successfully ....",customerRole.getId(),customerRole.getRoleName());
	    
	        User user = new User();
	        user.setEmail(request.getEmail());
	        user.setMobileNumber(request.getMobileNumber());
	        user.setPassword(passwordEncoder.encode(request.getPasswordHash()));
	        user.setEnabled(true);
	        if(rollName!=null) {
	        	user.setStatus(UserStatus.PENDING);
	        }
	        else {
	            user.setStatus(UserStatus.APPROVED); // Buyers can shop instantly
	        }
	        
//	        user.setEmailVerified(false);
//	        user.setPhoneVerified(false);
	        user.getRoles().add(customerRole);
	       
	        
	        List<Address> addressList = request.getAddress();
	        if (addressList != null) {
	            for (Address address : addressList) {
	                address.setUser(user);
	            }
	        }
	    	user.setAddress(addressList);
	        userRepository.save(user);
	    }
	    
	    
	    @org.springframework.transaction.annotation.Transactional
	    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	    	User user= (User) userRepository.findByEmail(email);
	    	
	    	
	    	
	    	if (user == null) {
	    		throw new UsernameNotFoundException("User not found");
	    	}
	    	
	    	//ALternnative in place of EAGER loading. If session is closed then getRole throw error
	    	org.hibernate.Hibernate.initialize(user.getRoles());
	    	
	    	String[] rolesArray = user.getRoles().stream()
                    .map(role -> role.getRoleName()) // Assuming `Role` has `getName()` method
                    .toArray(String[]::new);
	    	
	    	return org.springframework.security.core.userdetails.User.builder()
	    			.username(user.getEmail())
	    			.password(user.getPassword())
	    			.disabled(!user.isEnabled())
	    			.roles(rolesArray).build();
	    }


	    // For custom login page
//		public String loginRequest(RegisterRequest request) {
//			User user= (User) userRepository.findByEmail(request.getEmail());
//			boolean isMatch=passwordEncoder.matches(request.getPasswordHash(), user.getPassword());
//			
//			if(!isMatch) {
//				throw new RuntimeException("Invalid Credential");
//			}
//			
//			return "Login Successful";
//		}


	    @org.springframework.transaction.annotation.Transactional
	    public UserDetails processOAuthPostLogin(String email) {
	        User user = (User) userRepository.findByEmail(email);
	       System.out.println("Inside processOauth user"+user);
	        if (user == null) {
	            Role customerRole = roleRepository.findByRoleName("BUYER");
	            if (customerRole == null) {
	                customerRole = new Role();
	                customerRole.setRoleName("BUYER");
	                roleRepository.save(customerRole);
	            }

	            user = new User();
	            user.setEmail(email);
	            user.setMobileNumber("000000" + (long) (Math.random() * 1000000000L)); // Dummy unique mobile
	            user.setPassword(passwordEncoder.encode("OAUTH_USER_" + java.util.UUID.randomUUID().toString()));
	            user.setEnabled(true);
//	            user.setEmailVerified(true);
//	            user.setPhoneVerified(false);
	            user.getRoles().add(customerRole);
	            System.out.println("Inside processOauth adding user"+user);

	            userRepository.save(user);
	        }
	        
	        // Ensure roles are initialized
	        org.hibernate.Hibernate.initialize(user.getRoles());

	        String[] rolesArray = user.getRoles().stream()
	                .map(role -> role.getRoleName())
	                .toArray(String[]::new);
	        System.out.println("Inside processOauth rolesArray"+rolesArray);

	        return org.springframework.security.core.userdetails.User.builder()
	                .username(user.getEmail())
	                .password(user.getPassword())
	                .disabled(!user.isEnabled())
	                .roles(rolesArray).build();
	    }
	    
}
