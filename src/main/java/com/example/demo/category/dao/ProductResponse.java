package com.example.demo.category.dao;

import com.fasterxml.jackson.databind.JsonNode;

public class ProductResponse {
	
	
    private Long id;
    private Long variantId;
    private byte[] image;
    private String productName;
    private Float discount;
    private Long offerPrice;
    private Long price;
    private JsonNode variantAttributes;
    
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
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
    
    public ProductResponse() {
    	
    }
    
	public ProductResponse(String productName, Float discount, Long offerPrice, Long price) {
		super();
		this.productName = productName;
		this.discount = discount;
		this.offerPrice = offerPrice;
		this.price = price;
	}
	public ProductResponse(String productName, Float discount, Long offerPrice, Long price,JsonNode variantAttributes) {
		super();
		this.productName = productName;
		this.discount = discount;
		this.offerPrice = offerPrice;
		this.price = price;
		this.variantAttributes=variantAttributes;
	}
	
	public ProductResponse(String productName) {
		super();
		this.productName = productName;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Float getDiscount() {
		return discount;
	}
	public void setDiscount(Float discount) {
		this.discount = discount;
	}
	public Long getOfferPrice() {
		return offerPrice;
	}
	public void setOfferPrice(Long offerPrice) {
		this.offerPrice = offerPrice;
	}
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public JsonNode getVariantAttributes() {
		return variantAttributes;
	}
	public void setVariantAttributes(JsonNode variantAttributes) {
		this.variantAttributes = variantAttributes;
	}
	
	
    
    

}
