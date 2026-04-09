package com.example.demo.auth.service;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.category.entity.Product;
import com.example.demo.category.entity.ProductVariants;
import com.example.demo.category.repository.ProductRepository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.auth.dao.DashboardResponse;
import com.example.demo.auth.entity.CustomUserDetails;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.SellerRepository;

@Service
public class SellerService {

    private final ProductRepository productRepository;	
	private final SellerRepository sellerRepository;


	public SellerService(SellerRepository sellerRepository,ProductRepository productRepository) {
		this.sellerRepository=sellerRepository;
		this.productRepository=productRepository;
	}

	
	public DashboardResponse getDashboard(Long sellerId) {
		List<Product> sellersProducts=productRepository.findBySellerId(sellerId);
//				.orElseThrow(()->new RuntimeException("Seller not found"));
		
		for(Product p:sellersProducts) {
			List<ProductVariants> productVariants=p.getProductVariants();
			for(ProductVariants productVariant:productVariants) {
				productVariant.getQuantity();
//				productVariant.
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
