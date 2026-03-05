package com.example.demo.cart.dao;

public class CartResponse {
	
	private Long id;
	private Long variantId;
	private Long userId;
	private int quantity;
	private String status;
	private String productName;
	private Long price;
	private Long offerPrice;
	private com.fasterxml.jackson.databind.JsonNode variantAttributes;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getVariantId() {
		return variantId;
	}
	public void setVariantId(Long variantId) {
		this.variantId = variantId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public Long getOfferPrice() {
		return offerPrice;
	}
	public void setOfferPrice(Long offerPrice) {
		this.offerPrice = offerPrice;
	}
	public com.fasterxml.jackson.databind.JsonNode getVariantAttributes() {
		return variantAttributes;
	}
	public void setVariantAttributes(com.fasterxml.jackson.databind.JsonNode variantAttributes) {
		this.variantAttributes = variantAttributes;
	}

}
