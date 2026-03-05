package com.example.demo.category.dao;

import com.fasterxml.jackson.databind.JsonNode;

public class PurchaseResponse {
	private String productName;
	private JsonNode variantAttributes;
	private Long quantity;
	private Long totalPrice;
	
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public JsonNode getVariantAttributes() {
		return variantAttributes;
	}
	public void setVarientAttributes(JsonNode varientAttributes) {
		this.variantAttributes = varientAttributes;
	}
	public Long getQuantity() {
		return quantity;
	}
	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}
	public Long getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Long totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	

}
