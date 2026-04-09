package com.example.demo.category.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.demo.category.dao.CategoryMapper;
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
	
	@Autowired
	private CategoryMapper categoryMapper;
	
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
		categoryResponse.setId(savedCategory.getId());
		
		return categoryResponse;	
	}

	@Cacheable("categories")
	public List<CategoryResponse> getAllCategory() {
		// This single query fetches ALL Categories from the DB at once
		List<Category> categoryList = categoryRepository.findAllWithSubCategories();
//		List<CategoryResponse> categoryResponseList = new ArrayList<>();
//		
//		for(Category category : categoryList) {
//			CategoryResponse categoryResponse = new CategoryResponse();
//			
//			categoryResponse.setId(category.getId());
//			categoryResponse.setCategoryName(category.getCategoryName());
//			
//			// We pass the pre-fetched subcategories from Memory (0 DB queries) instead of asking the DB again
//			categoryResponse.setSubCategoryResponse(mapSubcategories(category.getSubCategories(), category.getCategoryName()));
//			
//			categoryResponseList.add(categoryResponse);
//		}
//		
//		return categoryResponseList;
		
		return categoryMapper.toCategoryResponseList(categoryList);
	}
	
	// Helper method to map the pre-fetched List<SubCategory> in memory without hitting the DB
	public List<SubCategoryResponse> mapSubcategories(List<SubCategory> subCategoryList, String categoryName) {
		List<SubCategoryResponse> subCategoryResponseList = new ArrayList<>();
		
		for(SubCategory subCategory : subCategoryList) {
			SubCategoryResponse subCategoryResponse = new SubCategoryResponse();
			subCategoryResponse.setCategoryName(categoryName);
			subCategoryResponse.setId(subCategory.getId());
			subCategoryResponse.setSpecificationName(subCategory.getSpecificationName());
			subCategoryResponse.setSubCategoryName(subCategory.getSubCategoryName());
			subCategoryResponseList.add(subCategoryResponse);
		}
		return subCategoryResponseList;
	}
	
	@Deprecated
	public List<SubCategoryResponse> getAllSubcategory(Long id){
		List<SubCategory> subCategoryList=subCategoryRepository.findByCategoryId(id);
		
		List<SubCategoryResponse> subCategoryResponseList=new ArrayList<>();
		
		for(SubCategory subCategory:subCategoryList) {
			SubCategoryResponse subCategoryResponse=new SubCategoryResponse();
			subCategoryResponse.setCategoryName(subCategory.getCategory().getCategoryName());
			subCategoryResponse.setId(subCategory.getId());
			subCategoryResponse.setSpecificationName(subCategory.getSpecificationName());
			subCategoryResponse.setSubCategoryName(subCategory.getSubCategoryName());
			subCategoryResponseList.add(subCategoryResponse);
			
		}
		return subCategoryResponseList;
	}

}
