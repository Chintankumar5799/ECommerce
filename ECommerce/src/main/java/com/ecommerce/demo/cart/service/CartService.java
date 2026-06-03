package com.ecommerce.demo.cart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.demo.auth.entity.User;
import com.ecommerce.demo.cart.dto.CartResponse;
import com.ecommerce.demo.cart.dto.CartStatus;
import com.ecommerce.demo.cart.entity.Cart;
import com.ecommerce.demo.cart.repository.CartRepository;
import com.ecommerce.demo.category.entity.ProductVariants;
import com.ecommerce.demo.config.SecurityConfig;
import com.ecommerce.demo.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

	private final CartRepository cartRepository;
	private static final Logger log = LoggerFactory.getLogger(CartService.class);

	CartService(CartRepository cartRepository) {
		this.cartRepository = cartRepository;
	}

	public CartResponse addToCart(Long userId, Long variantId, int quantity) {
		Cart cart = new Cart();
		Optional<Cart> existingProduct = cartRepository.findCartByUserIdAndVariantId(userId, variantId);
		if (existingProduct.isPresent()) {
			cart = existingProduct.get();

			if (cart.getStatus() == CartStatus.CART) {
				cart.setQuantity(cart.getQuantity() + quantity);
			} else {
				cart.setStatus(CartStatus.CART);
				cart.setQuantity(quantity);
			}
		} else {
			// cart = new Cart();

			User user = new User();
			user.setId(userId);

			ProductVariants productVariants = new ProductVariants();
			productVariants.setVariantId(variantId);

			cart.setUser(user);
			cart.setVariant(productVariants);
			cart.setQuantity(quantity);
			cart.setStatus(CartStatus.CART);

		}

		Cart savedProduct = cartRepository.save(cart);
		if (savedProduct == null) {
			log.warn("product with variantid not successfully saved {} for userId {}", variantId, userId);
		}
		CartResponse cartResponse = new CartResponse();
		cartResponse.setId(savedProduct.getId());
		cartResponse.setUserId(savedProduct.getUser().getId());
		cartResponse.setQuantity(savedProduct.getQuantity());
		cartResponse.setVariantId(savedProduct.getVariant().getVariantId());

		return cartResponse;
	}

	public CartResponse addToWishlist(Long userId, Long variantId, int quantity) {
		Cart cart = new Cart();
		Optional<Cart> existingProduct = cartRepository.findCartByUserIdAndVariantId(userId, variantId);

		if (existingProduct.isPresent()) {
			cart = existingProduct.get();

			if (cart.getStatus() == CartStatus.WISHLIST) {
				cart.setQuantity(cart.getQuantity() + quantity);
			} else {
				cart.setStatus(CartStatus.WISHLIST);
				cart.setQuantity(quantity);
			}
		} else {
			User user = new User();
			user.setId(userId);

			ProductVariants productVariants = new ProductVariants();
			productVariants.setVariantId(variantId);

			cart.setUser(user);
			cart.setVariant(productVariants);
			cart.setQuantity(quantity);
			cart.setStatus(CartStatus.WISHLIST);

		}

		Cart savedProduct = cartRepository.save(cart);
		CartResponse cartResponse = new CartResponse();
		cartResponse.setId(savedProduct.getId());
		cartResponse.setUserId(savedProduct.getUser().getId());
		cartResponse.setQuantity(savedProduct.getQuantity());
		cartResponse.setVariantId(savedProduct.getVariant().getVariantId());

		return cartResponse;
	}

	public CartResponse changeStatus(Long userId, Long variantId) {
		Cart cart = cartRepository.findCartByUserIdAndVariantId(userId, variantId)
				.orElseThrow(() -> new ResourceNotFoundException("No product found"));

		log.info("Inside service class to change status of product with userId {} and variantId {} ", userId,
				variantId + " status " + cart.getStatus());
		if (cart.getStatus() == CartStatus.CART) {
			cart.setStatus(CartStatus.WISHLIST);
		} else if (cart.getStatus() == CartStatus.WISHLIST) {
			cart.setStatus(CartStatus.CART);
		}

		Cart updatedStatus = cartRepository.save(cart);
		log.info("Inside updateStatus {}", updatedStatus);
		CartResponse cartResponse = new CartResponse();
		cartResponse.setId(updatedStatus.getId());
		cartResponse.setUserId(updatedStatus.getUser().getId());
		cartResponse.setQuantity(updatedStatus.getQuantity());
		cartResponse.setVariantId(updatedStatus.getVariant().getVariantId());

		return cartResponse;
	}

	@Transactional(readOnly = true)
	public Page<CartResponse> getCartDetails(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Cart> cartPage = cartRepository.findByUserId(userId, pageable);

		return cartPage.map(cart -> {
			CartResponse cartResponse = new CartResponse();
			cartResponse.setId(cart.getId());
			cartResponse.setQuantity(cart.getQuantity());
			cartResponse.setStatus(cart.getStatus());
			cartResponse.setUserId(cart.getUser().getId());

			ProductVariants variant = cart.getVariant();
			if (variant != null) {
				cartResponse.setVariantId(variant.getVariantId());
				cartResponse.setPrice(variant.getPrice());
				cartResponse.setOfferPrice(variant.getOfferPrice());
				cartResponse.setVariantAttributes(variant.getVariantAttributes());
				if (variant.getProduct() != null) {
					cartResponse.setProductName(variant.getProduct().getProductName());
				}
			}

			return cartResponse;
		});
	}

	@Transactional
	public String removeItem(Long userId, Long variantId) {
		cartRepository.deleteByUserIdAndVariantVariantId(userId, variantId);
		return "Item removed Successfully";
	}
}
