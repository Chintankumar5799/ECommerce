package com.example.demo.category.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.category.dao.CategoryResponse;
import com.example.demo.category.entity.Category;
import com.example.demo.category.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
	
	private final CategoryService categoryService;
	
	public CategoryController(CategoryService categoryService) {
		this.categoryService=categoryService;
	}
	
	@GetMapping("/getHi")
	public String getHi() {
		return "Hello askjnas";
	}
	
	//May be for admin only
	@PreAuthorize("hasRole('SELLER')")
	@PostMapping("/newCategory")
	public ResponseEntity<CategoryResponse> addCategory( @RequestParam String categoryName){
		CategoryResponse categoryResponse=categoryService.addCategory(categoryName);
		return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponse);
	}

	//For admin and seller
	@GetMapping("/getCategory")
	public ResponseEntity<List<CategoryResponse>> getAllCategory(){
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(categoryService.getAllCategory()); 
	}
	

}
