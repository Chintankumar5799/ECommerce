package com.example.demo.ai;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

@Service
public class DatatransformerImpl implements DataTransformer {

	// This convert read data to Chunks
	@Override
	public List<Document> transform(List<Document> documents) {
		var splitter=new TokenTextSplitter(30,40,10,5000,true);
		return splitter.transform(documents);
	}

}
