package com.example.demo.cart.service;
import com.example.demo.cart.repository.CartRepository;
import com.example.demo.category.entity.ProductVariants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.auth.config.SecurityConfig;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.exception.ResourceNotFoundException;
import com.example.demo.cart.dao.CartResponse;
import com.example.demo.cart.dao.CartStatus;
import com.example.demo.cart.entity.Cart;

@Service
public class CartService {

    private final CartRepository cartRepository;
	private static final Logger log = LoggerFactory.getLogger(CartService.class);
    
    CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

	public CartResponse addToCart(Long userId, Long variantId, int quantity) {
	
		Cart cart=new Cart();

		User user=new User();
		user.setId(userId);
		
		ProductVariants productVariants=new ProductVariants();
		productVariants.setVariantId(variantId);
		
		cart.setUser(user);
		cart.setVariant(productVariants);
		cart.setQuantity(quantity);
		cart.setStatus(CartStatus.CART);
		Cart savedProduct=cartRepository.save(cart);
		if(savedProduct==null) {
			log.warn("product with variantid not successfully saved {} for userId {}",variantId,userId);
		}
		CartResponse cartResponse=new CartResponse();
		cartResponse.setId(savedProduct.getId());
		cartResponse.setUserId(savedProduct.getUser().getId());
		cartResponse.setQuantity(savedProduct.getQuantity());
		cartResponse.setVariantId(savedProduct.getVariant().getVariantId());
		
		return cartResponse;
	}

	public CartResponse addToWishlist(Long userId, Long variantId, int quantity) {
		Cart cart=new Cart();
		
		User user=new User();
		user.setId(userId);
		
		ProductVariants productVariants=new ProductVariants();
		productVariants.setVariantId(variantId);
		
		cart.setUser(user);
		cart.setVariant(productVariants);
		cart.setQuantity(quantity);
		cart.setStatus(CartStatus.WISHLIST);
		Cart savedProduct=cartRepository.save(cart);
		
		CartResponse cartResponse=new CartResponse();
		cartResponse.setId(savedProduct.getId());
		cartResponse.setUserId(savedProduct.getUser().getId());
		cartResponse.setQuantity(savedProduct.getQuantity());
		cartResponse.setVariantId(savedProduct.getVariant().getVariantId());
		
		
		return cartResponse;
	}

	public CartResponse changeStatus(Long userId, Long variantId) {
		Cart cart=cartRepository.findCartByUserIdAndVariantId(userId,variantId)
				.orElseThrow(()->new ResourceNotFoundException("No product found"));
		
		System.out.println("Inside service"+cart.getStatus());
		if(cart.getStatus() == CartStatus.CART) {
			cart.setStatus(CartStatus.WISHLIST);
		}
		else if(cart.getStatus() == CartStatus.WISHLIST)
		{
			cart.setStatus(CartStatus.CART);
		}
		
		Cart updatedStatus=cartRepository.save(cart);
		System.out.println("Inside updateStatus"+updatedStatus);
		CartResponse cartResponse=new CartResponse();
		cartResponse.setId(updatedStatus.getId());
		cartResponse.setUserId(updatedStatus.getUser().getId());
		cartResponse.setQuantity(updatedStatus.getQuantity());
		cartResponse.setVariantId(updatedStatus.getVariant().getVariantId());
		
		return cartResponse;
	}

	public List<CartResponse> getCartDetails(Long userId) {
		List<Cart> cartList=Optional.of(cartRepository.findByUserId(userId)).filter(list->!list.isEmpty())
				.orElseThrow(()->new ResourceNotFoundException("Cart is empty"));
		
		List<CartResponse> cartReponseList= new ArrayList<>();
		
		for(Cart cart:cartList) {
			CartResponse cartResponse=new CartResponse();
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
			
			cartReponseList.add(cartResponse);
		}
		
		return cartReponseList;
	}

	public String removeItem(Long id) {
		cartRepository.deleteById(id);
		return "Item removed Successfully";
	}
}
