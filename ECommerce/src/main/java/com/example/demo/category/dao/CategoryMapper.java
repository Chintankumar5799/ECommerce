package com.example.demo.category.dao;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.category.entity.Category;
import com.example.demo.category.entity.SubCategory;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "subCategories", target = "subCategoryResponse")
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(source = "category.categoryName", target = "categoryName")
    SubCategoryResponse toSubCategoryResponse(SubCategory subCategory);
    
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);
}