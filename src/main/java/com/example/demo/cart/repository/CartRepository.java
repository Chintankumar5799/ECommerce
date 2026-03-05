package com.example.demo.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.cart.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>{
		@Query("Select e from Cart e where e.user.id = :userId AND e.variant.variantId = :variantId")
		Optional<Cart> findCartByUserIdAndVariantId(@Param("userId") Long userId, @Param("variantId") Long variantId);

		List<Cart> findByUserId(Long userId);
}
