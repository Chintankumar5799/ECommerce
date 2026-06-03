package com.ecommerce.demo.ai;

import java.util.List;

import org.springframework.ai.document.Document;

public interface DataTransformer {
	
	List<Document> transform(List<Document> documents);

}
