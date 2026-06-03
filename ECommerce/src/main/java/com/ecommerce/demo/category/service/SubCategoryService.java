package com.ecommerce.demo.category.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.demo.category.repository.CategoryRepository;
import com.ecommerce.demo.category.repository.SubCategoryRepository;
import com.ecommerce.demo.exception.ResourceNotFoundException;
import com.ecommerce.demo.category.dto.SubCategoryRequest;
import com.ecommerce.demo.category.dto.SubCategoryResponse;
import com.ecommerce.demo.category.entity.Category;
import com.ecommerce.demo.category.entity.SubCategory;

@Service
public class SubCategoryService {

	private final SubCategoryRepository subCategoryRepository;
	private final CategoryRepository categoryRepository;

	public SubCategoryService(SubCategoryRepository subCategoryRepository, CategoryRepository categoryRepository) {
		this.subCategoryRepository = subCategoryRepository;
		this.categoryRepository = categoryRepository;
	}

	private final static Logger log = LoggerFactory.getLogger(SubCategoryService.class);

	public SubCategoryResponse addSubCategory(SubCategoryRequest subCategoryRequest) {

		log.info("Sub category with category id and specific name is " + subCategoryRequest.getCategoryId() + " "
				+ subCategoryRequest.getSpecificationName());

		Category category = categoryRepository.findById(subCategoryRequest.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		SubCategory subcategory = new SubCategory();
		subcategory.setSubCategoryName(subCategoryRequest.getSubCategoryName());
		subcategory.setCategory(category);
		subcategory.setSpecificationName(subCategoryRequest.getSpecificationName());
		// subcategory.setId(subCategoryRequest.getId());

		SubCategory subCategorySaved = subCategoryRepository.save(subcategory);
		log.info("Sub category is saved");

		SubCategoryResponse subCategoryResponse = new SubCategoryResponse();
		subCategoryResponse.setCategoryName(category.getCategoryName());
		subCategoryResponse.setSpecificationName(subCategorySaved.getSpecificationName());
		subCategoryResponse.setSubCategoryName(subCategorySaved.getSubCategoryName());
		subCategoryResponse.setId(subCategorySaved.getId());

		return subCategoryResponse;

	}

}
