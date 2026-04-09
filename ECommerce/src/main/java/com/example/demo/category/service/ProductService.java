package com.example.demo.category.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.demo.category.dao.ProductRequest;
import com.example.demo.category.dao.ProductResponse;
import com.example.demo.category.dao.ProductVariantRequest;
import com.example.demo.category.dao.PurchaseResponse;
import com.example.demo.category.entity.Category;
import com.example.demo.category.entity.Images;
import com.example.demo.category.entity.Product;
import com.example.demo.category.entity.ProductVariants;
import com.example.demo.category.entity.SubCategory;
import com.example.demo.category.repository.ProductRepository;
import com.example.demo.category.repository.ProductVariantsRepository;
import com.example.demo.category.repository.SubCategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.data.jpa.domain.Specification;

@Service
public class ProductService {
	
	private final ProductRepository productRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final ProductVariantsRepository productVariantsRepository;
	
	public ProductService(ProductRepository productRepository, SubCategoryRepository subCategoryRepository,ProductVariantsRepository productVariantsRepository) {
		this.productRepository=productRepository;
		this.subCategoryRepository=subCategoryRepository;
		this.productVariantsRepository=productVariantsRepository;
	}

	
	@Transactional
	public ProductResponse addProduct(ProductRequest productRequest, JsonNode jsonAttributes, MultipartFile[] images) throws IOException {
	    if (productRequest.getSubCategoryId() == null) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sub Category ID is required");
	    }
	    
	    // Retrieve subcategory by ID
	    SubCategory subCategory = subCategoryRepository.findById(productRequest.getSubCategoryId())
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub Category not found"));
	    System.out.println(subCategory);
	    List<Images> imageList = new ArrayList<>();
	    Product product = new Product();

	    product.setProductName(productRequest.getProductName());
	    product.setDiscount(productRequest.getDiscount());
	    product.setOfferPrice(productRequest.getOfferPrice());
	    product.setPrice(productRequest.getPrice());
	    product.setSubCategory(subCategory);

	    // Process images if any
	    if (images != null && images.length > 0) {
	        for (MultipartFile file : images) {
	            Images img = new Images();
	            img.setImage(file.getBytes());  // Convert the image to byte[]
	            img.setProduct(product);        // Set the relation with the product
	            imageList.add(img);
	        }
	    }

	    // Set the images list to the product
	    product.setImages(imageList);

	    // Set the jsonAttributes (already parsed as JsonNode) to the product
	    product.setJsonAttributes(jsonAttributes);

	    // Save the product to the database
	    System.out.println("DEBUG: Saving Product: " + product);
	    Product productSaved=productRepository.save(product);
	    
//	    productVariantsRepository.generateVariants(productSaved.getId());
	    
	    if(productRequest.getVariants()!=null && !productRequest.getVariants().isEmpty()) {
	    	for(ProductVariantRequest proVariant:productRequest.getVariants()) {
	    		ProductVariants productVariants=new ProductVariants();
	    		productVariants.setDiscount(proVariant.getDiscount()!=null ? proVariant.getDiscount():0f);
	    		productVariants.setOfferPrice(proVariant.getOfferPrice()!=null? proVariant.getOfferPrice():0L);
	    		productVariants.setPrice(proVariant.getPrice()!=null? proVariant.getPrice():0L);
	    		productVariants.setProduct(productSaved);
	    		productVariants.setQuantity(proVariant.getQuantity()!=null? proVariant.getQuantity():0L);
	    		productVariants.setVariant_attributes(proVariant.getVariantAttributes());
	    		
	    		productVariantsRepository.save(productVariants);
	    	}
	    }
	    
	    ProductResponse productResponse=new ProductResponse();
	    productResponse.setDiscount(productSaved.getDiscount());
	    productResponse.setOfferPrice(productSaved.getOfferPrice());
	    productResponse.setProductName(productSaved.getProductName());
	    productResponse.setPrice(productSaved.getPrice());
	    
	
	    return productResponse;
	}


	@Transactional
	public PurchaseResponse buyProduct(Long variantId, Long quantity) {
		try {
			if (variantId == null || quantity == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "variantId and quantity are required");
			}
			
			
			int updatedStock=productVariantsRepository.decreaseStock(variantId, quantity);
			System.out.println("DEBUG: Starting buyProduct for variantId: " + variantId + ", quantity: " + quantity);
			
			if (updatedStock == 0) {
				System.out.println("ERROR: Variant exists but product is NULL for variantId: " + variantId);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock or variant not found");
			}
			
			ProductVariants productVariants = productVariantsRepository.findById(variantId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This variant not found with ID: " + variantId));
			
			
			
			Product product = productVariants.getProduct();
			System.out.println("DEBUG: Found Product: " + product.getProductName());
	
			
			PurchaseResponse purchaseResponse = new PurchaseResponse();
			purchaseResponse.setProductName(product.getProductName());
			purchaseResponse.setQuantity(quantity);
			purchaseResponse.setTotalPrice(quantity * productVariants.getPrice());
			// Convert JsonNode -> plain Java Object so Jackson serializes the actual JSON, not node metadata
			Object plainAttributes = null;
			try {
				if (productVariants.getVariantAttributes() != null) {
					// Round-trip through String to force plain Java Collections (Map/List)
					// instead of a JsonNode object that causes metadata serialization issues.
					plainAttributes = new ObjectMapper().readValue(
						productVariants.getVariantAttributes().toString(), 
						Object.class
					);
				}
			} catch (Exception ex) {
				System.out.println("WARN: Could not convert variantAttributes JsonNode: " + ex.getMessage());
			}
			purchaseResponse.setVarientAttributes(plainAttributes);
			
			System.out.println("DEBUG: PurchaseResponse prepared successfully for: " + purchaseResponse.getProductName());
			
			System.out.println("DEBUG: PurchaseResponse prepared successfully for: " + purchaseResponse.getVariantAttributes());
			
			return purchaseResponse;
		} catch (Exception e) {
			System.out.println("CRITICAL ERROR in buyProduct:");
			e.printStackTrace();
			throw e;
		}
	}
	
	public List<ProductResponse> getProductBySubCategory(Long subCategoryId) {
		try {
		List<Product> productList=productRepository.findBySubCategoryId(subCategoryId);
		List<ProductResponse> productResponseList = new ArrayList<>();
		
		for(Product product:productList) {
			ProductResponse productResponse=new ProductResponse();
			productResponse.setDiscount(product.getDiscount());
			productResponse.setOfferPrice(product.getOfferPrice());
			productResponse.setPrice(product.getPrice());
			productResponse.setProductName(product.getProductName());
			productResponse.setId(product.getId());
			
			productResponseList.add(productResponse);
		}
		
		System.out.println(productList.toString());
		return productResponseList;
		}catch(Exception e) {
			System.out.println("CRITICAL ERROR in buyProduct:");
			e.printStackTrace();
			throw e;
		}
	}


	public List<Product> getAllProducts() {
		List<Product> productList=productRepository.findAll();
		return productList;
	}
	
	
	public Page<ProductResponse> filterProducts(
			Long categoryId,
			Long subCategoryId,
			Long minPrice,
			Long maxPrice,
			int page, int size, String sortBy, String direction){
	
	Specification<Product> spec =(root, query, cb)->null;
	System.out.println("Inside filterProducts");
	if(categoryId!=null) {
		spec=spec.and((root, query, cb)-> cb.equal(root.get("subCategory")
														.get("category")
														.get("id"), categoryId));
	}
	
	if(subCategoryId != null) {
		spec=spec.and((root, query, cb)-> cb.equal(root.get("subCategory")
				.get("id"), subCategoryId));
	}
	
	if(minPrice != null) {
		spec=spec.and((root, query, cb)-> cb.greaterThanOrEqualTo(root.get("offerPrice"), minPrice));
	}
	
	if(maxPrice != null) {
		spec=spec.and((root, query, cb)-> cb.lessThanOrEqualTo(root.get("offerPrice"), maxPrice));
	}
	
	System.out.println("finish filterProducts");
	
	//Pagination
	Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
	System.out.println("direction "+sortDirection);
	Pageable pageable=PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
	System.out.println("pageable "+pageable);
	Page<Product> product=  productRepository.findAll(spec,pageable);
	
	Page<ProductResponse> productResponse=product.map(productEntity->{
		return new ProductResponse(
				productEntity.getProductName(),
				productEntity.getDiscount(),
			    productEntity.getOfferPrice(),
			    productEntity.getPrice()
			);
				
	});
	
	return productResponse;
   }


	public List<ProductResponse> getProductsBySubCategoryId(long subcategoryId) {
		List<Product> productList=productRepository.findBySubCategoryId(subcategoryId);
		
		List<ProductResponse> productResponseList=new ArrayList<>();
		
		for(Product product:productList) {
			ProductResponse response=new ProductResponse();
			response.setProductName(product.getProductName());
			response.setId(product.getId());
			response.setPrice(product.getPrice());
			response.setOfferPrice(product.getOfferPrice());
			response.setDiscount(product.getDiscount());
			productResponseList.add(response);
			
		}
		return productResponseList;
	}


	public List<ProductResponse> getProductsVariantByProductId(Long productId) {
		List<ProductVariants> productVariantsList=productVariantsRepository.findByProductId(productId);
		List<ProductResponse> productResponseList=new ArrayList<>();
		
		for(ProductVariants productVariants:productVariantsList) {
			ProductResponse productResponse=new ProductResponse();
			productResponse.setId(productVariants.getVariantId());
			productResponse.setOfferPrice(productVariants.getOfferPrice());
			productResponse.setDiscount(productVariants.getDiscount());
			productResponse.setPrice(productVariants.getPrice());
			System.out.println("Price"+productVariants.getPrice()+" Offer Price"+productVariants.getOfferPrice());
			productResponse.setVariantId(productVariants.getVariantId());
			
			// Convert JsonNode -> plain Java Object so Jackson serializes the actual JSON
			Object plainAttributes = null;
			try {
				if (productVariants.getVariantAttributes() != null) {
					plainAttributes = new ObjectMapper().readValue(
						productVariants.getVariantAttributes().toString(), 
						Object.class
					);
				}
			} catch (Exception ex) {
				System.out.println("WARN: Could not convert variantAttributes JsonNode: " + ex.getMessage());
			}
			
			System.out.println("Plain attributes..."+plainAttributes);
			productResponse.setVariantAttributes(plainAttributes);
			
			productResponseList.add(productResponse);
		}
		
		return productResponseList;
	}


	public String removeProducts(Long productId) {
		productRepository.deleteById(productId);
		return "Product is removed";
	}


}
