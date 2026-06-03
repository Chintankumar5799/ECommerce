package com.ecommerce.demo.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.demo.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	User findByEmail(String email);

}
