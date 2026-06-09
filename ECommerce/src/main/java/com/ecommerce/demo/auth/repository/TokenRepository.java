package com.ecommerce.demo.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.demo.config.VerificationToken;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
}
