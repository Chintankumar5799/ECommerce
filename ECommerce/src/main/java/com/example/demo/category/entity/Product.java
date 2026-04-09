package com.example.demo.category.entity;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.demo.auth.entity.BaseEntity;
import com.example.demo.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="product")
public class Product extends BaseEntity{
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable=false)
    private String productName;

	@Column(name="deleted",nullable = false)
    private Boolean isDeleted=false;


    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Images> images;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariants> productVariants;

//    @Lob
//    @Column(columnDefinition = "json")
//    private List<String> specification;
//    Both above and below approach works but below approach not need to serialize and de-Serialize manually
//    but only for Dbl like postgres where jsonb datatype is there.
    
	 @JdbcTypeCode(SqlTypes.JSON)
	 private JsonNode jsonAttributes;
	    
	    
	 @ManyToOne
	 @JoinColumn(name = "seller_id", nullable = false)
	 @JsonIgnore
	 private User seller;
	    
	 
	 private Long price;
	 private Long offerPrice;
	 private float discount;
//	    @JsonIgnore
//	    @OneToMany
//	    private ProductVariants productVariants;
	    
	    

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public SubCategory getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(SubCategory subCategory) {
		this.subCategory = subCategory;
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

	public List<Images> getImages() {
		return images;
	}

	public void setImages(List<Images> images) {
		this.images = images;
	}

	public JsonNode getJsonAttributes() {
		return jsonAttributes;
	}

	public void setJsonAttributes(JsonNode jsonAttributes) {
		this.jsonAttributes = jsonAttributes;
	}

	public User getSeller() {
		return seller;
	}

	public void setSeller(User seller) {
		this.seller = seller;
	}

	public List<ProductVariants> getProductVariants() {
		return productVariants;
	}

	public void setProductVariants(List<ProductVariants> productVariants) {
		this.productVariants = productVariants;
	}
    
	
    
}
