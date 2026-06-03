package com.ecommerce.demo.category.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecommerce.demo.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.subCategories")
	List<Category> findAllWithSubCategories();

	

}
