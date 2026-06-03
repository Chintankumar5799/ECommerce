package com.ecommerce.demo.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.demo.auth.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

	Role findByRoleName(String string);
	
}
