package com.example.demo.category.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.category.dao.SubCategoryRequest;
import com.example.demo.category.dao.SubCategoryResponse;
import com.example.demo.category.entity.Category;
import com.example.demo.category.entity.SubCategory;
import com.example.demo.category.service.CategoryService;
import com.example.demo.category.service.SubCategoryService;

import jakarta.validation.Valid;

import com.example.demo.category.service.SubCategoryService;

@RestController
@RequestMapping("/api/subcategory")
public class SubCategoryController {


	private final SubCategoryService subCategoryService;
	
	public SubCategoryController(SubCategoryService subCategoryService) {
		this.subCategoryService=subCategoryService;
	}
	
	@GetMapping("/getHi")
	public String getHi() {
		return "Hello askjnas";
	}
	
	@PostMapping("/newSubCategory")
	public ResponseEntity<SubCategoryResponse> addSubCategory(@Valid @RequestBody SubCategoryRequest subCategoryRequest){
		SubCategoryResponse subCategory=subCategoryService.addSubCategory(subCategoryRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(subCategory);
	}
	
	
//	@PostMapping("/getSubcategory")
//	public ResponseEntity<P> getSubcategory(@categoryId){
//		
//	}
}
