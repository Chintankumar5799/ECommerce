package com.example.demo.category.dao;

import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import com.example.demo.category.entity.Images;
import com.example.demo.category.entity.SubCategory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

public class ProductRequest {
	 @NotBlank(message="Name of product is not empty")
	 private String productName;

	//  @NotBlank(message="Name of product is not empty")
	 private Long subCategoryId;
	   
	//  @NotBlank(message="Name of product is not empty")
	  private long price;
	  private long offerPrice;
	  private float discount;
	    
	  private List<ProductVariantRequest> variants;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Long getSubCategoryId() {
		return subCategoryId;
	}

	public void setSubCategoryId(Long subCategoryId) {
		this.subCategoryId = subCategoryId;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public long getOfferPrice() {
		return offerPrice;
	}

	public void setOfferPrice(long offerPrice) {
		this.offerPrice = offerPrice;
	}

	public float getDiscount() {
		return discount;
	}

	public void setDiscount(float discount) {
		this.discount = discount;
	}

	public List<ProductVariantRequest> getVariants() {
		return variants;
	}

	public void setVariants(List<ProductVariantRequest> variants) {
		this.variants = variants;
	}
	  
	  
}
