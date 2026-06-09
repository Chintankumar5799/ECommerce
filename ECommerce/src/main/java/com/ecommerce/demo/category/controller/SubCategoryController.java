package com.ecommerce.demo.category.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.demo.category.dto.SubCategoryRequest;
import com.ecommerce.demo.category.dto.SubCategoryResponse;
import com.ecommerce.demo.category.entity.Category;
import com.ecommerce.demo.category.entity.SubCategory;
import com.ecommerce.demo.category.service.CategoryService;
import com.ecommerce.demo.category.service.SubCategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subcategory")
public class SubCategoryController {

	private final SubCategoryService subCategoryService;

	private final static Logger log = LoggerFactory.getLogger(SubCategoryController.class);

	public SubCategoryController(SubCategoryService subCategoryService) {
		this.subCategoryService = subCategoryService;
	}

	@GetMapping("/getHi")
	public String getHi() {
		return "Hello askjnas";
	}

	@PostMapping("/v1/newSubCategory")
	public ResponseEntity<SubCategoryResponse> addSubCategory(
			@Valid @RequestBody SubCategoryRequest subCategoryRequest) {
		SubCategoryResponse subCategory = subCategoryService.addSubCategory(subCategoryRequest);
		log.info("Add subcategory with name" + subCategory.toString());
		return ResponseEntity.status(HttpStatus.CREATED).body(subCategory);
	}

	// @PostMapping("/getSubcategory")
	// public ResponseEntity<P> getSubcategory(@categoryId){
	//
	// }
}
