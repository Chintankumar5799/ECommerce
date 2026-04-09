package com.example.demo.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.category.entity.Product;
import com.example.demo.category.entity.SubCategory;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long>{

	@Query("SELECT e FROM SubCategory e WHERE e.category.id = :categoryId")
	List<SubCategory> findByCategoryId(@Param("categoryId") Long categoryId);

}
