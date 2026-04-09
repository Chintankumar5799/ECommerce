package com.example.demo.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.dao.DashboardResponse;
import com.example.demo.auth.entity.CustomUserDetails;
import com.example.demo.auth.entity.Dashboard;
import com.example.demo.auth.repository.SellerRepository;
import com.example.demo.auth.service.SellerService;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

	private final SellerService sellerService;
	private final SellerRepository sellerRepository;
	
	public SellerController(SellerService sellerService, SellerRepository sellerRepository) {
		this.sellerService=sellerService;
		this.sellerRepository=sellerRepository;
	}
	
	
//	@GetMapping("/dashboard")
//	public DashboardResponse getDashboard(java.security.Principal principal) {
//		return sellerService.getDashboard(principal.getName());
//	}
//	
}
