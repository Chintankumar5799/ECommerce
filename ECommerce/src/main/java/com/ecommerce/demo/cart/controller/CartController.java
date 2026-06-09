package com.ecommerce.demo.cart.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.demo.auth.dto.CustomUserPrincipal;
import com.ecommerce.demo.cart.dto.CartResponse;
import com.ecommerce.demo.cart.service.CartService;
import com.ecommerce.demo.config.SecurityConfig;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cartService;

	private static final Logger log = LoggerFactory.getLogger(CartController.class);

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@PostMapping("/v1/addToCart")
	public ResponseEntity<CartResponse> addToCart(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam Long variantId,
			@RequestParam int quantity) {

		Long userId = principal.getUserId();
		log.info("User {} added variantId {} with {} quantity in Cart.", userId, variantId, quantity);
		CartResponse cartResponse = cartService.addToCart(userId, variantId, quantity);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}

	@PostMapping("/v1/wishlist")
	public ResponseEntity<CartResponse> addToWishlist(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam Long variantId,
			@RequestParam int quantity) {

		Long userId = principal.getUserId();
		log.info("User {} added, variantId {} with {} quantity in Wishlist", userId, variantId, quantity);
		CartResponse cartResponse = cartService.addToWishlist(userId, variantId, quantity);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}

	@PutMapping("/v1/changeStatus")
	public ResponseEntity<CartResponse> changeStatus(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam Long variantId) {

		Long userId = principal.getUserId();
		log.info("User {} change status of variantId {}", userId, variantId);
		CartResponse cartResponse = cartService.changeStatus(userId, variantId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}

	@GetMapping("/v1/getCart")
	public ResponseEntity<Page<CartResponse>> getCartDetails(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam int page, @RequestParam int size) {
		Long userId = principal.getUserId();
		log.info("User {} just get details of Cart.", userId);
		Page<CartResponse> cartResponse = cartService.getCartDetails(userId, page, size);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}

	@DeleteMapping("/v1/remove")
	public ResponseEntity<String> removeFromCart(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam Long variantId) {

		Long userId = principal.getUserId();
		log.info("User {} remove item {} ", userId, variantId);
		String remove = cartService.removeItem(userId, variantId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Item remove Successfully");
	}

	// @GetMapping("/getWishlist")
	// public ResponseEntity<CartResponse> getWishlistDetails(@RequestParam Long
	// userId, @RequestParam Long variantId, @RequestParam int quantity){
	// CartResponse cartResponse=cartService.getWishlistDetails(userId, variantId,
	// quantity);
	// return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	// }

}
