package com.ecommerce.demo.category.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.demo.category.entity.Product;
// import com.ecommerce.demo.category.entity.SubCategory;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
		JpaSpecificationExecutor<Product> {

	@Query("SELECT e FROM Product e WHERE e.subCategory.id = :subCategoryId")
	Page<Product> findBySubCategoryId(
			@Param("subCategoryId") Long subCategoryId,
			Pageable pageable);
	// List<Product> findBySubCategoryId(@Param("subCategoryId") Long
	// subCategoryId);

	List<Product> findBySellerId(Long selllerId);

	// @Query("select e from product e where subcategoryId=:subcategoryId")
	// Product getProductsBySubCategoryId(@Param("subcategoryId") Long
	// subcategoryId);

}
