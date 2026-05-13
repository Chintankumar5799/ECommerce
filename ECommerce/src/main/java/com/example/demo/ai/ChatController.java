package com.example.demo.ai;

import com.example.demo.cart.service.CartService;
import com.example.demo.helper.Helper;

import reactor.core.publisher.Flux;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/AI")
public class ChatController {

    private final ChatService chatService;
    private final DataLoaderImpl dataLoaderImpl;

    // Call using Service
    public ChatController(ChatService chatService, DataLoaderImpl dataLoaderImpl) {
        this.chatService = chatService;
        this.dataLoaderImpl=dataLoaderImpl;
    }

    @GetMapping("/chatBox")
    public ResponseEntity<String> chatBox(@RequestParam(value = "q",required = true) String q,
    									@RequestHeader("userId") String userId) {
    	String resultResponse=chatService.chatBox(q,userId);
    	return ResponseEntity.ok(resultResponse);
    }
    
    @GetMapping("/streamingChatBox")
    public ResponseEntity<Flux<String>> streamingChatBox(@RequestParam(value = "q",required = true) String q) {
    	
    	Flux<String> resultResponse=chatService.streamingChatBox(q);
    	return ResponseEntity.ok(resultResponse);
    }
    
    @GetMapping("/saveData")
    public ResponseEntity<String> streamingChatBox() {
    	
    	String resultResponse=chatService.saveData(Helper.getData());
    	return ResponseEntity.ok("Data Saved");
    }
    
    @GetMapping("/getResponse")
    public ResponseEntity<String> getResponse(String q) {
    	
    	String getResponse=chatService.getResponse(q);
    	return ResponseEntity.ok(getResponse);
    }

    @GetMapping("/jsonDataLoader")
    public String getJsonDataLoader() {
    	List<Document> document=dataLoaderImpl.loadDocumentsFromJson();
    	return "Json data is loaded";
    }
    
    @GetMapping("/pdfDataLoader")
    public String getPdfDataLoader() {
    	List<Document> documents=dataLoaderImpl.loadDocumentsFromPdf();
    	
    	documents.forEach(item->{
    		System.out.println(item);
    	});
    	return "Pdf data is loaded";
    }
    
//    ----------------other small apis
    @GetMapping("/chatTool")
    public ResponseEntity<String> chatTool(@RequestParam(value = "q",required = true) String q) {
    	String resultResponse=chatService.chatTool(q);
    	return ResponseEntity.ok(resultResponse);
    }

    // --------- Directly call -----------------

    // private final ChatClient openAiChatClient;
    // private final ChatClient ollamaChatClient;

    // public ChatController( //@Qualifier("openAiChatClient") ChatClient
    // openAiChatModel,
    // @Qualifier("ollamaChatClient") ChatClient ollmaChatModel) {
    //// this.openAiChatClient=openAiChatModel;
    // this.ollamaChatClient=ollmaChatModel;
    // }

    // Below is for client we go one step above to model, because we have multiple
    // model
    // public ChatController(ChatClient.Builder builder) {
    // this.chatClient = builder.build();
    // }

    // @GetMapping("/chatBox")
    // public ResponseEntity<String> chat(@RequestParam(value = "q", required =
    // true) String q) {
    // var resultResponse = ollamaChatClient.prompt(q).call().content();
    // return ResponseEntity.ok(resultResponse);
    //// var resultResponse = openAiChatClient.prompt(q).call().content();
    //// return ResponseEntity.ok(resultResponse);
    // }

}