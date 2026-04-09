package com.example.demo.category.dao;

public class PurchaseResponse {
	private String productName;
	private Object variantAttributes;
	private Long quantity;
	private Long totalPrice;
	
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Object getVariantAttributes() {
		return variantAttributes;
	}
	public void setVarientAttributes(Object varientAttributes) {
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
