package com.example.demo.category.dao;

import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import com.example.demo.category.entity.Category;
import com.example.demo.category.entity.Product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

public class SubCategoryRequest {
	

	@NotBlank(message = "Sub-Category name is required")
    private String subCategoryName;

	private Long id;
//	@NotNull(message = "Category of Sub-Category is required")
    private Long categoryId;

    private String specificationName;

    
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubCategoryName() {
		return subCategoryName;
	}

	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getSpecificationName() {
		return specificationName;
	}

	public void setSpecificationName(String specificationName) {
		this.specificationName = specificationName;
	}

	
}
