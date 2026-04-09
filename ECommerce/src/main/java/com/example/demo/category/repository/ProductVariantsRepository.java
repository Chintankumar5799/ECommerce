package com.example.demo.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.category.entity.ProductVariants;

import jakarta.transaction.Transactional;

@Repository
public interface ProductVariantsRepository extends JpaRepository<ProductVariants, Long> {
	  @Modifying
	  @Transactional
	  @Query(value = "CALL public.generate_variants(:productId)", nativeQuery = true)
	  void generateVariants(@Param("productId") Long productId);
	  
	  
	  @Modifying
	  @Transactional
	  @Query("UPDATE ProductVariants v SET v.quantity = v.quantity - :qty WHERE v.id = :variantId AND v.quantity >= :qty")
	  int decreaseStock(@Param("variantId") Long variantId, @Param("qty") Long qty);

	  @Query("SELECT v FROM ProductVariants v WHERE v.product.id = :productId")
	  List<ProductVariants> findByProductId(@Param("productId") Long productId);
	  
	
	  

}
