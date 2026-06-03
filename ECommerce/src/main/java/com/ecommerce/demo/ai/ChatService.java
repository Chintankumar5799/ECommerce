package com.ecommerce.demo.ai;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
//import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

	public ChatClient chatClient;

	private VectorStore vectorStore;

	@Value("classpath:/prompts/user-message.st")
	private Resource userMessage;

	@Value("classpath:/prompts/system-message.st")
	private Resource systemMessage;

	private final SimpleDateTimeTool simpleDateTimeTool;

	public ChatService(ChatClient chatClient, VectorStore vectorStore, SimpleDateTimeTool simpleDateTimeTool) {
		this.chatClient = chatClient;
		this.vectorStore = vectorStore;
		this.simpleDateTimeTool = simpleDateTimeTool;
	}

	// public String chatBox(String q) {
	//
	// Prompt prompt1 = new Prompt(q);
	//
	// String queryStr = "As an expert in java programming";
	//
	// // List<Tut> tutorial = chatClient
	// // .prompt(q)
	// // .call()
	// // .entity(new ParameterizedTypeReference<List<Tut>>() {
	// // });
	// // .entity(Tut.class);
	//
	// var tutorial = chatClient
	// .prompt()
	// .user(u -> u.text(queryStr).param("query", queryStr))
	// .call()
	// .content();
	//
	// // .getResult().getOutput().getText();
	// // return content;
	//
	// return tutorial;
	// }

	// ------------- using template
	// public String chatBox(String q) {
	// //First step
	// PromptTemplate strTemplate= PromptTemplate.builder().template("What is
	// {techName}? tell me example {exampleName}").build();
	//
	// //render the template
	// String renderedMessage=strTemplate.render(Map.of(
	// "techName","Spring",
	// "exampleName","Spring boot"
	// ));
	//
	// Prompt prompt=new Prompt(renderedMessage);
	//
	// return this.chatClient.prompt(prompt).call().content();
	//
	// }

	// ----------- Use file for prompt
	// public String chatBox(String q) {
	// return this.chatClient
	// .prompt()
	//// .advisors(new SimpleLoggerAdvisor())
	// .system(system->
	// system.text(systemMessage))
	// .user(user->
	// user.text(userMessage)
	// .param("concept",q))
	// .call()
	// .content();
	// }

	// For separating user sessions
	// public String chatBox(String q,String userId) {
	// return this.chatClient
	// .prompt()
	// .advisors(advisorSpec-> advisorSpec.param(ChatMemory.CONVERSATION_ID,
	// userId))
	// .system(system->
	// system.text(systemMessage))
	// .user(user->
	// user.text(userMessage)
	// .param("concept",q))
	// .call()
	// .content();
	// }

	//// same as above but with vectorDB and similarity search
	// public String chatBox(String q,String userId) {
	//
	//// SearchRequest searchRequest=SearchRequest.builder()
	//// .query(q)
	//// .topK(5)
	//// .similarityThreshold(0.6)
	//// .build();
	////
	//// List<Document> documents=this.vectorStore.similaritySearch(searchRequest);
	//// List<String>
	//// documentList=documents.stream().map(Document::getText).toList();
	//// String contextData=String.join(",", documentList);
	////
	//
	// return this.chatClient
	// .prompt()
	// // .advisors(advisorSpec-> advisorSpec.param(ChatMemory.CONVERSATION_ID,
	//// userId))
	//// .system(system->
	//// system.text(systemMessage).param("documents", contextData))
	//// .advisors(new QuestionAnswerAdvisor(vectorStore))
	// .advisors(QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.5).build()).build())
	// .user(user->
	// user.text(userMessage)
	// .param("concept",q))
	// .call()
	// .content();
	// }

	// same as above with pre and post retrivel option also for actual DOCUMENT
	public String chatBox(String q, String userId) {

		var advisor = RetrievalAugmentationAdvisor
				.builder()
				.documentRetriever(VectorStoreDocumentRetriever
						.builder()
						.vectorStore(this.vectorStore)
						.topK(3)
						.similarityThreshold(0.5)
						.build())
				.queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
				.build();

		return this.chatClient
				.prompt()
				.advisors(advisor)
				.user(user -> user.text(userMessage)
						.param("concept", q))
				.call()
				.content();
	}

	public Flux<String> streamingChatBox(String q) {

		return this.chatClient
				.prompt()
				.system(system -> system.text(this.systemMessage))
				.user(user -> user.text(this.userMessage).param("concept", q))
				.stream()
				.content();
	}

	public String saveData(List<String> list) {
		try {
			List<Document> documentList = list.stream().map(Document::new).toList();
			this.vectorStore.add(documentList);
			return "Data successfully saved.";
		} catch (Exception e) {
			return "Data already exists in the database. (Skipped duplicate save)";
		}
	}

	// Same as above but retrival, preretrival and post retrival conditions
	public String getResponse(String userQuery) {

		var advisor = RetrievalAugmentationAdvisor.builder()

				// Pre-retrival Phase
				.queryTransformers(
						RewriteQueryTransformer.builder()
								.chatClientBuilder(chatClient.mutate().clone())
								.build())
				.queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClient.mutate().clone()).build())
				// Retrival Phase
				.documentRetriever(
						VectorStoreDocumentRetriever.builder()
								.vectorStore(vectorStore)
								.topK(3)
								.similarityThreshold(0.3)
								.build())

				// Post retrival
				.documentJoiner(new ConcatenationDocumentJoiner())
				.queryAugmenter(ContextualQueryAugmenter.builder().build())
				.build();

		return chatClient
				.prompt(userQuery)
				.advisors(advisor)
				.call()
				.content();
	}

	public String chatTool(String q) {
		return chatClient
				.prompt()
				.tools(new SimpleDateTimeTool())
				.user(q).call().content();
	}
}
