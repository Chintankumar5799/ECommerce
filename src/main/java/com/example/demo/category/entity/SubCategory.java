package com.example.demo.category.entity;

import java.util.List;

import com.example.demo.auth.entity.BaseEntity;
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
@Table(name="subcategory")
public class SubCategory extends BaseEntity{
	
	  	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private long id;

	  	@Column(nullable=false)
	    private String subCategoryName;

		@Column(name="deleted",nullable = false)
	    private Boolean isDeleted=false;

	    @ManyToOne
	    @JsonIgnore
	    @JoinColumn(name = "category_id")
	    private Category category;
	    
	    

	    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<Product> products;  // Corrected to List<Product> (One-to-many relationship)

	    @Column
	    private String specificationName;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getSubCategoryName() {
			return subCategoryName;
		}

		public void setSubCategoryName(String subCategoryName) {
			this.subCategoryName = subCategoryName;
		}

		public Boolean getIsDeleted() {
			return isDeleted;
		}

		public void setIsDeleted(Boolean isDeleted) {
			this.isDeleted = isDeleted;
		}

		public Category getCategory() {
			return category;
		}

		public void setCategory(Category category) {
			this.category = category;
		}

		public List<Product> getProducts() {
			return products;
		}

		public void setProducts(List<Product> products) {
			this.products = products;
		}

		public String getSpecificationName() {
			return specificationName;
		}

		public void setSpecificationName(String specificationName) {
			this.specificationName = specificationName;
		}
	    
	    
	    
	
}
