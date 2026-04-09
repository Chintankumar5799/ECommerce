package com.example.demo.category.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import com.example.demo.category.dao.ProductRequest;
import com.example.demo.category.dao.ProductResponse;
import com.example.demo.category.dao.PurchaseResponse;
import com.example.demo.category.entity.Category;
import com.example.demo.category.entity.Product;
import com.example.demo.category.entity.SubCategory;
import com.example.demo.category.service.ProductService;

@RestController
@RequestMapping("/api/product")
public class ProductController {
	
	private final ProductService productService;
	private final ObjectMapper objectMapper;

	public ProductController(ProductService productService) {
		this.productService=productService;
		this.objectMapper = new ObjectMapper();
	}
	

	@PreAuthorize("hasRole('SELLER')")
	@PostMapping(value="/newProduct",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> addProduct(
	        @RequestPart("productRequest") String requestStr,
			@RequestPart("jsonAttributes") String jsonAttributes, 
	      @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException{
		
		System.out.println("DEBUG raw JSON requestStr: " + requestStr);
		
		ProductRequest request = null;
		try {
			request = objectMapper.readValue(requestStr, ProductRequest.class);
		} catch (Exception e) {
			System.out.println("ERROR: JSON Parsing failed for productRequest");
			e.printStackTrace();
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format for productRequest");
		}
		
		JsonNode jsonNode = null;
		try {
		    jsonNode = objectMapper.readTree(jsonAttributes);  // Convert JSON string to JsonNode
		    System.out.println("DEBUG parsed jsonAttributes as JsonNode: " + jsonNode.toString());
		} catch (Exception e) {
		    System.out.println("ERROR: Failed to parse jsonAttributes JSON");
		    e.printStackTrace();
		}
		    
		System.out.println("DEBUG: Parsed Product Name: " + request.getProductName());
		System.out.println("DEBUG: Parsed SubCategory ID: " + request.getSubCategoryId());

		ProductResponse productResponse=productService.addProduct(request,jsonNode,images);
		return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
	}

	//BUYER 
	@GetMapping("/getProduct")
	public ResponseEntity<List<ProductResponse>> getProduct(@RequestParam Long subcategoryId){
		System.out.println("DEBUG: Parsed SubCategory ID: " + subcategoryId);
		List<ProductResponse> productResponse=productService.getProductsBySubCategoryId(subcategoryId);
		return ResponseEntity.status(HttpStatus.OK).body(productResponse);
	}
	
	//BUYER
	@GetMapping("/getProductBySubCategory")
	public ResponseEntity<List<ProductResponse>> getProductBySubCategory(@RequestParam Long subCategoryId){
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(productService.getProductBySubCategory(subCategoryId));
		
	}

	//BUYER
	@GetMapping("/getProductVariant")
	public ResponseEntity<List<ProductResponse>> getSubcategory(@RequestParam Long productId){
		System.out.println("DEBUG: Parsed Product ID: " + productId);
		List<ProductResponse> productResponse=productService.getProductsVariantByProductId(productId);
	    return ResponseEntity.status(HttpStatus.OK).body(productResponse);
	}
	
	
	//BUYER
	@PostMapping("/buyProduct")
	public ResponseEntity<PurchaseResponse> buyProduct(@RequestParam Long variantId,@RequestParam Long quantity){
		
		System.out.println(variantId+" "+quantity);
		
		PurchaseResponse purchaseResponse=productService.buyProduct(variantId,quantity);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(purchaseResponse);
	}
	
	
	@PostMapping("/filter")
	public ResponseEntity<Page<ProductResponse>> getFilteredProducts(@RequestParam Long categoryId,
			@RequestParam Long subCategoryId,@RequestParam Long minPrice, @RequestParam Long maxPrice,
			@RequestParam int page,@RequestParam int size,@RequestParam String sortBy,@RequestParam String direction){
		
//		FilterService filterService=new FilterService(categoryId,subCategoryId,minPrice,maxPrice);
//		System.out.println(categoryId+" "+subCategoryId);
		
//		List<Product> allProducts=productService.getAllProducts();
//		System.out.println(allProducts);	
//		List<Product> filteredProducts=filterService.applyFilters(allProducts);
		
		Page<ProductResponse> filteredProducts=productService.filterProducts(categoryId, subCategoryId, minPrice, maxPrice, page, size, sortBy, direction);
		System.out.println(filteredProducts);
		return ResponseEntity.ok(filteredProducts);
		
	}
	
	
	@DeleteMapping
	public ResponseEntity<String> removeProduct(@RequestParam Long productId){
		String remove=productService.removeProducts(productId);
		return ResponseEntity.status(HttpStatus.OK).body(remove);
	}
	
	
	
	
}
