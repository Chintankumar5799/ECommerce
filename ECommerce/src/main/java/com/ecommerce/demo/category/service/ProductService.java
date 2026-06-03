package com.ecommerce.demo.category.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.ecommerce.demo.category.entity.Category;
import com.ecommerce.demo.category.entity.Images;
import com.ecommerce.demo.category.entity.Product;
import com.ecommerce.demo.category.entity.ProductVariants;
import com.ecommerce.demo.category.entity.SubCategory;
import com.ecommerce.demo.category.repository.ProductRepository;
import com.ecommerce.demo.category.repository.ProductVariantsRepository;
import com.ecommerce.demo.category.repository.SubCategoryRepository;
import com.ecommerce.demo.category.controller.ProductController;
import com.ecommerce.demo.category.dto.ProductRequest;
import com.ecommerce.demo.category.dto.ProductResponse;
import com.ecommerce.demo.category.dto.ProductVariantRequest;
import com.ecommerce.demo.category.dto.PurchaseResponse;

import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.demo.auth.entity.User;
import com.ecommerce.demo.auth.repository.UserRepository;
//import org.springframework.data.jpa.domain.Specification;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final ProductVariantsRepository productVariantsRepository;
	private final UserRepository userRepository;

	private final static Logger log = LoggerFactory.getLogger(ProductService.class);

	public ProductService(ProductRepository productRepository, SubCategoryRepository subCategoryRepository,
			ProductVariantsRepository productVariantsRepository, UserRepository userRepository) {
		this.productRepository = productRepository;
		this.subCategoryRepository = subCategoryRepository;
		this.productVariantsRepository = productVariantsRepository;
		this.userRepository = userRepository;

	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public ProductResponse addProduct(Long sellerId, ProductRequest productRequest, JsonNode jsonAttributes,
			MultipartFile[] images)
			throws IOException {
		if (productRequest.getSubCategoryId() == null) {
			log.error("Sub-category {} is not found.", productRequest.getSubCategoryId());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sub Category ID is required");
		}

		// Retrieve subcategory by ID
		SubCategory subCategory = subCategoryRepository.findById(productRequest.getSubCategoryId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub Category not found"));

		User seller = userRepository.findById(sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

		List<Images> imageList = new ArrayList<>();
		Product product = new Product();

		product.setProductName(productRequest.getProductName());
		product.setDiscount(productRequest.getDiscount());
		product.setOfferPrice(productRequest.getOfferPrice());
		product.setPrice(productRequest.getPrice());
		product.setSubCategory(subCategory);
		product.setSeller(seller);

		// Process images if any
		if (images != null && images.length > 0) {
			for (MultipartFile file : images) {
				Images img = new Images();
				img.setImage(file.getBytes()); // Convert the image to byte[]
				img.setProduct(product); // Set the relation with the product
				imageList.add(img);
			}
		}

		// Set the images list to the product
		product.setImages(imageList);

		// Set the jsonAttributes (already parsed as JsonNode) to the product
		product.setJsonAttributes(jsonAttributes);

		// Save the product to the database
		log.info("Saving Product: {}", product);
		Product productSaved = productRepository.save(product);

		// productVariantsRepository.generateVariants(productSaved.getId());

		if (productRequest.getVariants() != null && !productRequest.getVariants().isEmpty()) {
			for (ProductVariantRequest proVariant : productRequest.getVariants()) {
				ProductVariants productVariants = new ProductVariants();
				productVariants.setDiscount(proVariant.getDiscount() != null ? proVariant.getDiscount() : 0f);
				productVariants.setOfferPrice(proVariant.getOfferPrice() != null ? proVariant.getOfferPrice() : 0L);
				productVariants.setPrice(proVariant.getPrice() != null ? proVariant.getPrice() : 0L);
				productVariants.setProduct(productSaved);
				productVariants.setQuantity(proVariant.getQuantity() != null ? proVariant.getQuantity() : 0L);
				productVariants.setVariantAttributes(proVariant.getVariantAttributes());

				log.info("Product variant {} is saved under product {}", productVariants.getVariantAttributes(),
						productVariants.getProduct());
				productVariantsRepository.save(productVariants);
			}
		}

		ProductResponse productResponse = new ProductResponse();
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

			int updatedStock = productVariantsRepository.decreaseStock(variantId, quantity);
			log.info("Starting buyProduct for variantId: {}, quantity: {}", variantId, quantity);

			if (updatedStock == 0) {
				log.error("Variant exists but product is NULL for variantId: {}", variantId);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock or variant not found");
			}

			ProductVariants productVariants = productVariantsRepository.findById(variantId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
							"This variant not found with ID: " + variantId));

			Product product = productVariants.getProduct();
			log.info("Product is found : " + product.getProductName());

			PurchaseResponse purchaseResponse = new PurchaseResponse();
			purchaseResponse.setProductName(product.getProductName());
			purchaseResponse.setQuantity(quantity);
			purchaseResponse.setTotalPrice(quantity * productVariants.getPrice());
			// Convert JsonNode -> plain Java Object so Jackson serializes the actual JSON,
			// not node metadata
			Object plainAttributes = null;
			try {
				if (productVariants.getVariantAttributes() != null) {
					// Round-trip through String to force plain Java Collections (Map/List)
					// instead of a JsonNode object that causes metadata serialization issues.
					plainAttributes = new ObjectMapper().readValue(
							productVariants.getVariantAttributes().toString(),
							Object.class);
				}
			} catch (Exception ex) {
				log.error("Could not convert variantAttributes JsonNode: {}", ex.getMessage());
			}
			purchaseResponse.setVarientAttributes(plainAttributes);

			log.info("PurchaseResponse prepared successfully for: {}", purchaseResponse.getProductName());

			log.info("PurchaseResponse prepared successfully for: {}", purchaseResponse.getVariantAttributes());

			return purchaseResponse;
		} catch (Exception e) {
			log.error("Error to buyProduct");
			// e.printStackTrace();
			throw e;
		}
	}

	public Page<ProductResponse> getProductBySubCategory(Long subCategoryId, int page, int size) {
		try {

			Pageable pageable = PageRequest.of(page, size);
			Page<Product> productPage = productRepository.findBySubCategoryId(subCategoryId, pageable);
			List<ProductResponse> productResponseList = new ArrayList<>();
			log.info("Get paginated products by sub category");

			return productPage.map(product -> {
				ProductResponse productResponse = new ProductResponse();
				productResponse.setDiscount(product.getDiscount());
				productResponse.setOfferPrice(product.getOfferPrice());
				productResponse.setPrice(product.getPrice());
				productResponse.setProductName(product.getProductName());
				productResponse.setId(product.getId());

				return productResponse;
			});

		} catch (Exception e) {
			log.error("Error to get Product by Sub category {}", e);
			// e.printStackTrace();
			throw e;
		}
	}

	@Transactional(readOnly = true)
	@Cacheable("products")
	public List<ProductResponse> getAllProducts() {
		List<Product> productList = productRepository.findAll();

		List<ProductResponse> allProducts = new ArrayList<>();

		for (Product product : productList) {
			ProductResponse productResponse = new ProductResponse();
			productResponse.setDiscount(product.getDiscount());
			productResponse.setOfferPrice(product.getOfferPrice());
			productResponse.setPrice(product.getPrice());
			productResponse.setProductName(product.getProductName());
			productResponse.setId(product.getId());

			allProducts.add(productResponse);
		}

		log.info("Getting list of all products");
		return allProducts;
	}

	public Page<ProductResponse> filterProducts(
			Long categoryId,
			Long subCategoryId,
			Long minPrice,
			Long maxPrice,
			int page, int size, String sortBy, String direction) {

		Specification<Product> spec = (root, query, cb) -> null;
		log.info("Inside filterProducts");
		if (categoryId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("subCategory")
					.get("category")
					.get("id"), categoryId));
		}

		if (subCategoryId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("subCategory")
					.get("id"), subCategoryId));
		}

		if (minPrice != null) {
			spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("offerPrice"), minPrice));
		}

		if (maxPrice != null) {
			spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("offerPrice"), maxPrice));
		}

		log.info("finish filterProducts");

		// Pagination
		Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
		log.info("direction : {} ", sortDirection);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
		log.info("pageable : {}", pageable);
		Page<Product> product = productRepository.findAll(spec, pageable);

		Page<ProductResponse> productResponse = product.map(productEntity -> {
			return new ProductResponse(
					productEntity.getProductName(),
					productEntity.getDiscount(),
					productEntity.getOfferPrice(),
					productEntity.getPrice());

		});

		return productResponse;
	}

	// public List<ProductResponse> getProductsBySubCategoryId(long subcategoryId) {
	// List<Product>
	// productList=productRepository.findBySubCategoryId(subcategoryId);
	//
	// List<ProductResponse> productResponseList=new ArrayList<>();
	//
	// for(Product product:productList) {
	// ProductResponse response=new ProductResponse();
	// response.setProductName(product.getProductName());
	// response.setId(product.getId());
	// response.setPrice(product.getPrice());
	// response.setOfferPrice(product.getOfferPrice());
	// response.setDiscount(product.getDiscount());
	// productResponseList.add(response);
	//
	// }
	// return productResponseList;
	// }

	public List<ProductResponse> getProductsVariantByProductId(Long productId) {
		List<ProductVariants> productVariantsList = productVariantsRepository.findByProductId(productId);
		List<ProductResponse> productResponseList = new ArrayList<>();

		for (ProductVariants productVariants : productVariantsList) {
			ProductResponse productResponse = new ProductResponse();
			productResponse.setId(productVariants.getVariantId());
			productResponse.setOfferPrice(productVariants.getOfferPrice());
			productResponse.setDiscount(productVariants.getDiscount());
			productResponse.setPrice(productVariants.getPrice());
			log.info("Price : {} Offer Price {} ", productVariants.getPrice(), productVariants.getOfferPrice());
			productResponse.setVariantId(productVariants.getVariantId());

			// Convert JsonNode -> plain Java Object so Jackson serializes the actual JSON
			Object plainAttributes = null;
			try {
				if (productVariants.getVariantAttributes() != null) {
					plainAttributes = new ObjectMapper().readValue(
							productVariants.getVariantAttributes().toString(),
							Object.class);
				}
			} catch (Exception ex) {
				log.error("Could not convert variantAttributes JsonNode: {}", ex.getMessage());
			}

			log.info("Plain attributes : {}", plainAttributes);
			productResponse.setVariantAttributes(plainAttributes);

			productResponseList.add(productResponse);
		}

		return productResponseList;
	}

	public String removeProducts(Long productId) {
		productRepository.deleteById(productId);
		log.info("Removing product with product id {}", productId);
		return "Product is removed";
	}

}
