package com.ecommerce.demo.category.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ecommerce.demo.category.repository.CategoryRepository;
import com.ecommerce.demo.category.repository.SubCategoryRepository;
import com.ecommerce.demo.category.dto.CategoryMapper;
import com.ecommerce.demo.category.dto.CategoryResponse;
import com.ecommerce.demo.category.dto.SubCategoryResponse;
import com.ecommerce.demo.category.entity.Category;
import com.ecommerce.demo.category.entity.SubCategory;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final CategoryMapper categoryMapper;

	private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

	public CategoryService(CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository,CategoryMapper categoryMapper) {
		this.categoryRepository = categoryRepository;
		this.subCategoryRepository = subCategoryRepository;
		this.categoryMapper=categoryMapper;
	}

	public CategoryResponse addCategory(String categoryName) {
		Category category = new Category();
		category.setCategoryName(categoryName);
		log.info("Saving new category to DB");
		Category savedCategory = categoryRepository.save(category);
		CategoryResponse categoryResponse = new CategoryResponse();
		categoryResponse.setCategoryName(savedCategory.getCategoryName());
		categoryResponse.setId(savedCategory.getId());
		log.info("Returning new category Response");

		return categoryResponse;
	}

	@Cacheable("categories")
	public List<CategoryResponse> getAllCategory() {
		// This single query fetches ALL Categories from the DB at once
		List<Category> categoryList = categoryRepository.findAllWithSubCategories();
		// List<CategoryResponse> categoryResponseList = new ArrayList<>();
		//
		// for(Category category : categoryList) {
		// CategoryResponse categoryResponse = new CategoryResponse();
		//
		// categoryResponse.setId(category.getId());
		// categoryResponse.setCategoryName(category.getCategoryName());
		//
		// // We pass the pre-fetched subcategories from Memory (0 DB queries) instead
		// of asking the DB again
		// categoryResponse.setSubCategoryResponse(mapSubcategories(category.getSubCategories(),
		// category.getCategoryName()));
		//
		// categoryResponseList.add(categoryResponse);
		// }
		//
		// return categoryResponseList;
		log.info("Get All Category list");
		return categoryMapper.toCategoryResponseList(categoryList);
	}

	// Helper method to map the pre-fetched List<SubCategory> in memory without
	// hitting the DB
	public List<SubCategoryResponse> mapSubcategories(List<SubCategory> subCategoryList, String categoryName) {
		List<SubCategoryResponse> subCategoryResponseList = new ArrayList<>();

		for (SubCategory subCategory : subCategoryList) {
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
	public List<SubCategoryResponse> getAllSubcategory(Long id) {
		List<SubCategory> subCategoryList = subCategoryRepository.findByCategoryId(id);
		log.info("Get all sub-category.");
		List<SubCategoryResponse> subCategoryResponseList = new ArrayList<>();

		for (SubCategory subCategory : subCategoryList) {
			SubCategoryResponse subCategoryResponse = new SubCategoryResponse();
			subCategoryResponse.setCategoryName(subCategory.getCategory().getCategoryName());
			subCategoryResponse.setId(subCategory.getId());
			subCategoryResponse.setSpecificationName(subCategory.getSpecificationName());
			subCategoryResponse.setSubCategoryName(subCategory.getSubCategoryName());
			subCategoryResponseList.add(subCategoryResponse);

		}
		return subCategoryResponseList;
	}

}
