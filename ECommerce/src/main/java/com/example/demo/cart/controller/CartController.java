package com.example.demo.cart.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.config.SecurityConfig;
import com.example.demo.cart.dao.CartResponse;
import com.example.demo.cart.service.CartService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
		this.cartService=cartService;
	}
	
	@PostMapping("/addToCart")
	public ResponseEntity<CartResponse> addToCart(@RequestParam Long userId, @RequestParam Long variantId, @RequestParam int quantity){
		log.info("User {} added variantId {} with {} quantity in Cart.",userId,variantId,quantity);
		CartResponse cartResponse=cartService.addToCart(userId, variantId, quantity);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}
	
	@PostMapping("/wishlist")
	public ResponseEntity<CartResponse> addToWishlist(@RequestParam Long userId, @RequestParam Long variantId, @RequestParam int quantity){
		log.info("User {} added, variantId {} with {} quantity in Wishlist",userId,variantId,quantity);
		CartResponse cartResponse=cartService.addToWishlist(userId, variantId, quantity);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}
	
	@PutMapping("/changeStatus")
	public ResponseEntity<CartResponse> changeStatus(@RequestParam Long userId, @RequestParam Long variantId){
        log.info("User {} change status of variantId {}",userId,variantId);
		CartResponse cartResponse=cartService.changeStatus(userId,variantId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}
	
	@GetMapping("/getCart")
	public ResponseEntity<List<CartResponse>> getCartDetails(@RequestParam Long userId){
		log.info("User {} just get details of Cart.",userId);
		List<CartResponse> cartResponse=cartService.getCartDetails(userId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
	}
	
	
	@DeleteMapping("/remove")
	public ResponseEntity<String> removeFromCart(@RequestParam Long id){
		log.info("User remove item {} ",id);
		String remove=cartService.removeItem(id);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Item remove Successfully");
	}
	
//	@GetMapping("/getWishlist")
//	public ResponseEntity<CartResponse> getWishlistDetails(@RequestParam Long userId, @RequestParam Long variantId, @RequestParam int quantity){
//		CartResponse cartResponse=cartService.getWishlistDetails(userId, variantId, quantity);
//		return ResponseEntity.status(HttpStatus.ACCEPTED).body(cartResponse);
//	}
	
	
	

}
