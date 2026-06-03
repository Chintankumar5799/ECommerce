package com.ecommerce.demo.ai;

import java.util.List;

import org.springframework.ai.document.Document;

public interface DataLoader {

	List<Document> loadDocumentsFromJson();
	
	List<Document> loadDocumentsFromPdf();
	
}
