package com.example.demo.category.filter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.category.entity.Product;

public class FilterService {
	
	private Long categoryId;
	private Long subCategoryId;
	private Long minPrice;
	private Long maxPrice;
	
	public FilterService(Long categoryId, Long subCategoryId, Long minPrice, Long maxPrice) {
		this.categoryId=categoryId;
		this.subCategoryId=subCategoryId;
		this.minPrice=minPrice;
		this.maxPrice=maxPrice;
	
	}
	


// Not for production level, same for above but in below logic first fetch all user but it take more time.	
//	public boolean findByCategory(Product product) {
//		if(categoryId==null) return true;
//		return product.getSubCategory().getCategory().getId()==categoryId;
//	}
//	
//	public boolean findBySubCategory(Product product) {
//		if(subCategoryId==null) return true;
//		return product.getSubCategory().getId()==subCategoryId;
//	}
//	
//	
//	public boolean findByPrice(Product product) {
//		//if((minPrice==0 || minPrice==null) && (maxPrice==0 || minPrice==null)) return true;
//		
//		if(minPrice==null && maxPrice==null) return true;
//		Long price=	product.getOfferPrice();
//		boolean result=(maxPrice==0 && price>=minPrice) || (minPrice==0 && price<=maxPrice)
//				|| (maxPrice!=0 && minPrice!=0 && price>=minPrice && price<=maxPrice) ;
//
//		return result;
//	}
//	
//	
//	public List<Product> applyFilters(List<Product> products){
//		return products.stream()
//				.filter(this::findByCategory)
//				.filter(this::findBySubCategory)
//				.filter(this::findByPrice)
//				.collect(Collectors.toList());
//	}
}
