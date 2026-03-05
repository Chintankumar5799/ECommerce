package com.example.demo.category.service;

import org.springframework.stereotype.Service;

import com.example.demo.category.dao.SubCategoryRequest;
import com.example.demo.category.dao.SubCategoryResponse;
import com.example.demo.category.entity.Category;
import com.example.demo.category.entity.SubCategory;
import com.example.demo.category.repository.CategoryRepository;
import com.example.demo.category.repository.SubCategoryRepository;

@Service
public class SubCategoryService {
	
	private final SubCategoryRepository subCategoryRepository;
	private final CategoryRepository categoryRepository;
	
	public SubCategoryService(SubCategoryRepository subCategoryRepository,CategoryRepository categoryRepository) {
		this.subCategoryRepository=subCategoryRepository;
		this.categoryRepository=categoryRepository;
	}
	
	
	public SubCategoryResponse addSubCategory(SubCategoryRequest subCategoryRequest) {
		
		
		System.out.println(subCategoryRequest.getCategoryId()+" "+subCategoryRequest.getSpecificationName());
		
		Category category=categoryRepository.findById(subCategoryRequest.getCategoryId())
				.orElseThrow(() -> new RuntimeException("Category not found"));
		
		SubCategory subcategory=new SubCategory();
		subcategory.setSubCategoryName(subCategoryRequest.getSubCategoryName());
		subcategory.setCategory(category);
		subcategory.setSpecificationName(subCategoryRequest.getSpecificationName());
		
		SubCategory subCategorySaved=subCategoryRepository.save(subcategory);
		
		SubCategoryResponse subCategoryResponse=new SubCategoryResponse();
		subCategoryResponse.setCategoryName(category.getCategoryName());
		subCategoryResponse.setSpecificationName(subCategorySaved.getSpecificationName());
		subCategoryResponse.setSubCategoryName(subCategorySaved.getSubCategoryName());
		
		return subCategoryResponse;
		
	}

}
