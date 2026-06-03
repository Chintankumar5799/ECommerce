package com.ecommerce.demo.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.demo.auth.entity.CustomUserDetails;
import com.ecommerce.demo.auth.entity.Dashboard;
import com.ecommerce.demo.auth.repository.SellerRepository;
import com.ecommerce.demo.auth.service.SellerService;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

	private final SellerService sellerService;
	private final SellerRepository sellerRepository;

	public SellerController(SellerService sellerService, SellerRepository sellerRepository) {
		this.sellerService = sellerService;
		this.sellerRepository = sellerRepository;
	}

	// @GetMapping("/dashboard")
	// public DashboardResponse getDashboard(java.security.Principal principal) {
	// return sellerService.getDashboard(principal.getName());
	// }
	//
}
