package com.example.demo.category.dao;

import java.util.List;

import com.example.demo.category.entity.SubCategory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;

public class CategoryResponse {
	
    private Long id;
    private String categoryName;
    private List<SubCategoryResponse> subCategoryResponse;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
    
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public List<SubCategoryResponse> getSubCategoryResponse() {
		return subCategoryResponse;
	}
	public void setSubCategoryResponse(List<SubCategoryResponse> subCategoryResponse) {
		this.subCategoryResponse = subCategoryResponse;
	}

}
