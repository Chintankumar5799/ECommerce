package com.example.demo.category.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.demo.auth.entity.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="product_variant")
public class ProductVariants extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long variantId;
	
	
	@ManyToOne
	@JoinColumn(name="productId")
	private Product product;
	
	@Column(nullable=false)
	private Long price;

	private Long offerPrice;

	private Float discount;
	
	private Long quantity;
	
	@Column(name="deleted")
	private Boolean isDeleted=false;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode variantAttributes;

	public long getVariantId() {
		return variantId;
	}

	public void setVariantId(long variantId) {
		this.variantId = variantId;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
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

	public Float getDiscount() {
		return discount;
	}

	public void setDiscount(Float discount) {
		this.discount = discount;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public JsonNode getVariantAttributes() {
		return variantAttributes;
	}

	public void setVariant_attributes(JsonNode variantAttributes) {
		this.variantAttributes = variantAttributes;
	}
	
	

}
