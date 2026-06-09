package com.ecommerce.demo.auth.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

import com.ecommerce.demo.auth.controller.AdminController;
import com.ecommerce.demo.auth.dto.RegisterRequest;
import com.ecommerce.demo.auth.entity.Address;
import com.ecommerce.demo.auth.entity.Role;
import com.ecommerce.demo.auth.entity.User;
import com.ecommerce.demo.auth.entity.UserStatus;
import com.ecommerce.demo.auth.repository.RoleRepository;
import com.ecommerce.demo.auth.repository.TokenRepository;
import com.ecommerce.demo.auth.repository.UserRepository;
import com.ecommerce.demo.config.VerificationToken;
import com.ecommerce.demo.exception.UserAlreadyExistsException;

//import org.springframework.security.core.userdetails.User;

@Service
public class UserService implements UserDetailsService {

	// private final ECommerceApplication ECommerceApplication;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final TokenRepository tokenRepository;
	private final MailSender mailSender;

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
			TokenRepository tokenRepository, MailSender mailSender) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.tokenRepository = tokenRepository;
		this.mailSender = mailSender;
	}

	/**
	 * @param request
	 * @param roleName
	 */
	@org.springframework.transaction.annotation.Transactional
	public void register(RegisterRequest request, String roleName) {
		if (userRepository.findByEmail(request.getEmail()) != null) {
			log.warn("User with {} is already Exists", request.getEmail());
			throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
		}

		Role customerRole = roleRepository.findByRoleName(roleName);

		log.info("Customer {} with mobile number {} register successfully ....", customerRole.getId(),
				customerRole.getRoleName());

		User user = new User();
		user.setEmail(request.getEmail());
		user.setMobileNumber(request.getMobileNumber());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setEnabled(true);
		if ("SELLER`".equals(roleName)) {
			user.setStatus(UserStatus.PENDING);
		} else {
			user.setStatus(UserStatus.APPROVED); // Buyers can shop instantly
		}

		user.setEmailVerified(false);
		// user.setPhoneVerified(false);
		user.getRoles().add(customerRole);

		List<Address> addressList = request.getAddress();
		if (addressList != null) {
			for (Address address : addressList) {
				address.setUser(user);
			}
		}
		user.setAddress(addressList);
		userRepository.save(user);

		// Email Verification code
		String token = UUID.randomUUID().toString();

		VerificationToken vToken = new VerificationToken();
		vToken.setToken(token);
		vToken.setUser(user);

		vToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
		tokenRepository.save(vToken);

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setSubject("Complete Registration!");
		mailMessage.setText("To confirm your account, please clieck here : " +
				"http://localhost:8081/api/auth/verify-email?token=" + token);

		mailSender.send(mailMessage);

	}

	@org.springframework.transaction.annotation.Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = (User) userRepository.findByEmail(email);

		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}

		// ALternnative in place of EAGER loading. If session is closed then getRole
		// throw error
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
	// public String loginRequest(RegisterRequest request) {
	// User user= (User) userRepository.findByEmail(request.getEmail());
	// boolean isMatch=passwordEncoder.matches(request.getPasswordHash(),
	// user.getPassword());
	//
	// if(!isMatch) {
	// throw new RuntimeException("Invalid Credential");
	// }
	//
	// return "Login Successful";
	// }

	@org.springframework.transaction.annotation.Transactional
	public UserDetails processOAuthPostLogin(String email) {
		User user = (User) userRepository.findByEmail(email);
		log.info("Inside processOauth user" + user);
		if (user == null) {
			Role customerRole = roleRepository.findByRoleName("BUYER");
			if (customerRole == null) {
				customerRole = new Role();
				customerRole.setRoleName("BUYER");
				roleRepository.save(customerRole);
			}

			user = new User();
			user.setEmail(email);
			// Generate a unique placeholder for mobile since OAuth doesn't provide it
			user.setMobileNumber(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 15));
			user.setPassword(passwordEncoder.encode("OAUTH_USER_" + java.util.UUID.randomUUID().toString()));
			user.setEnabled(true);
			// user.setEmailVerified(true);
			// user.setPhoneVerified(false);
			user.getRoles().add(customerRole);
			log.info("Inside processOauth adding user" + user);

			userRepository.save(user);
		}

		// Ensure roles are initialized
		org.hibernate.Hibernate.initialize(user.getRoles());

		String[] rolesArray = user.getRoles().stream()
				.map(role -> role.getRoleName())
				.toArray(String[]::new);
		log.info("Inside processOauth rolesArray" + rolesArray);

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.disabled(!user.isEnabled())
				.roles(rolesArray).build();
	}

	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public User findByEmail(String email) {

		log.info("Fetching user by email: {}", email);
		return userRepository.findByEmail(email);
	}

}
