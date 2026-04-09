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
@Table(name="category")
public class Category extends BaseEntity {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private long id;

	    @Column(nullable=false)
	    private String categoryName;

	    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	    @JsonIgnore
	    private List<SubCategory> subCategories;

		@Column(name="deleted",nullable = false)
	    private Boolean isDeleted=false;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public List<SubCategory> getSubCategories() {
			return subCategories;
		}

		public void setSubCategories(List<SubCategory> subCategories) {
			this.subCategories = subCategories;
		}

		public Boolean getIsDeleted() {
			return isDeleted;
		}

		public void setIsDeleted(Boolean isDeleted) {
			this.isDeleted = isDeleted;
		}

		
	    
}
