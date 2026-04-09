package com.example.demo.order.entity;

import com.example.demo.category.entity.Product;
import com.example.demo.category.entity.ProductVariants;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="orders_items")
public class OrderItem {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="order_id")
    @JsonIgnore
    private Order order;
	
	@ManyToOne
	@JoinColumn(name="product_id")
    @JsonIgnore
    private Product product;
	
	@ManyToOne
	@JoinColumn(name="product_variant_id")
    @JsonIgnore
    private ProductVariants variant;

	@Column
    private Integer quantity;
	@Column
    private Long price;        // price at time of purchase
	@Column
    private Long totalPrice;
	
	@Column(name="product_variant_name")
	private String productVariantName;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public ProductVariants getVariant() {
		return variant;
	}
	public void setVariant(ProductVariants variant) {
		this.variant = variant;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public Long getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Long totalPrice) {
		this.totalPrice = totalPrice;
	}
	public String getProductVariantName() {
		return productVariantName;
	}
	public void setProductVariantName(String productVariantName) {
		this.productVariantName = productVariantName;
	}
	
      
}
