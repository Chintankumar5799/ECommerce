package com.example.demo.auth.service;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.category.entity.Product;
import com.example.demo.category.entity.ProductVariants;
import com.example.demo.category.repository.ProductRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.auth.config.OAuthSuccessHandler;
import com.example.demo.auth.dao.DashboardResponse;
import com.example.demo.auth.entity.CustomUserDetails;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.SellerRepository;

@Service
public class SellerService {

    private final ProductRepository productRepository;	
	private final SellerRepository sellerRepository;


	private static final Logger log = LoggerFactory.getLogger(SellerService.class);
	
	public SellerService(SellerRepository sellerRepository,ProductRepository productRepository) {
		this.sellerRepository=sellerRepository;
		this.productRepository=productRepository;
	}

	
	public DashboardResponse getDashboard(Long sellerId) {
		List<Product> sellersProducts=productRepository.findBySellerId(sellerId);
//				.orElseThrow(()->new RuntimeException("Seller not found"));
		log.info("Dashboard for seller "+sellerId);
		for(Product p:sellersProducts) {
			List<ProductVariants> productVariants=p.getProductVariants();
			for(ProductVariants productVariant:productVariants) {
				productVariant.getQuantity();

			}
		}
		
		DashboardResponse response = new DashboardResponse();
		response.setApproved(true);
		response.setPendingOrders(0);
		response.setTotalProducts(0);
		response.setTotalRevenue(0);
		
		return response;
//		return new CustomUserDetails(
//				user.getId(), user.getEmail(), user.getRoles());
	}
	
}
