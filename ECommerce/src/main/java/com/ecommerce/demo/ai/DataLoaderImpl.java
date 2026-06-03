package com.ecommerce.demo.ai;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DataLoaderImpl implements DataLoader{
	
	
	
	@Value("classpath:docker_commands.json")
	private Resource jsonResource;
	
	@Value("classpath:aws_commands.pdf")
	private Resource awsResource;
	
	
	@Autowired
	private VectorStore vectorStore;
	
	@Autowired
	private DatatransformerImpl datatransformerImpl;
	
	@Override
	public List<Document> loadDocumentsFromJson() {
		var jsonReader=new JsonReader(jsonResource);
		var listDocuments=jsonReader.read();
		return listDocuments;
	}
	
	//All include in Spring AI Document ETL pipeline

	@Override
	public List<Document> loadDocumentsFromPdf() {
		
		var pdfReader=new PagePdfDocumentReader(awsResource,
				PdfDocumentReaderConfig.builder()
				.withPageTopMargin(0)
				.withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
						.withNumberOfTopPagesToSkipBeforeDelete(0)
						.build())
				.withPagesPerDocument(1)
				.build()
				);
		
		//To convert above data to chunk
		var transformedDocument=this.datatransformerImpl.transform(pdfReader.read());
		
		this.vectorStore.add(transformedDocument);
			
		return pdfReader.read();
		
	     }

}
