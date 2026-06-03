package com.ecommerce.demo.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.demo.category.entity.Images;


@Repository
public interface ImagesRepository extends JpaRepository<Images, Long> {

}
