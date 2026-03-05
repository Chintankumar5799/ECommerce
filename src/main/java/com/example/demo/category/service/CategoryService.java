package com.example.demo.category.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.category.dao.CategoryResponse;
import com.example.demo.category.dao.SubCategoryResponse;
import com.example.demo.category.entity.Category;
import com.example.demo.category.entity.SubCategory;
import com.example.demo.category.repository.CategoryRepository;
import com.example.demo.category.repository.SubCategoryRepository;



@Service
public class CategoryService {
	
	private CategoryRepository categoryRepository;
	private SubCategoryRepository subCategoryRepository;
	
	public CategoryService(CategoryRepository categoryRepository,SubCategoryRepository subCategoryRepository) {
		this.categoryRepository=categoryRepository;
		this.subCategoryRepository=subCategoryRepository;
	}

	public CategoryResponse addCategory(String categoryName) {
		Category category=new Category();
		category.setCategoryName(categoryName);
		
		Category savedCategory=categoryRepository.save(category);
		CategoryResponse categoryResponse=new CategoryResponse();
		categoryResponse.setCategoryName(savedCategory.getCategoryName());
		
		return categoryResponse;	
	}

	public List<CategoryResponse> getAllCategory() {
		List<Category> categoryList=categoryRepository.findAll();
		
		List<CategoryResponse> categoryResponseList=new ArrayList<>();
		
		for(Category category:categoryList) {
			CategoryResponse categoryResponse=new CategoryResponse();
			
			categoryResponse.setId(category.getId());
			categoryResponse.setCategoryName(category.getCategoryName());
			categoryResponse.setSubCategoryResponse(getAllSubcategory(category.getId()));
			categoryResponseList.add(categoryResponse);
		}
		
		return categoryResponseList;
	}
	
	public List<SubCategoryResponse> getAllSubcategory(Long id){
		List<SubCategory> subCategoryList=subCategoryRepository.findByCategoryId(id);
		
		List<SubCategoryResponse> subCategoryResponseList=new ArrayList<>();
		
		for(SubCategory subCategory:subCategoryList) {
			SubCategoryResponse subCategoryResponse=new SubCategoryResponse();
//			subCategoryResponse.setCategoryName(subCategory.getCategory());
			subCategoryResponse.setId(subCategory.getId());
			subCategoryResponse.setSpecificationName(subCategory.getSpecificationName());
			subCategoryResponse.setSubCategoryName(subCategory.getSubCategoryName());
			subCategoryResponseList.add(subCategoryResponse);
			
		}
		return subCategoryResponseList;
	}

}
