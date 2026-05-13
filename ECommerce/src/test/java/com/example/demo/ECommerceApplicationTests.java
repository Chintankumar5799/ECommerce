package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.ai.ChatService;
import com.example.demo.helper.Helper;

@SpringBootTest
class ECommerceApplicationTests {

	@Test
	void contextLoads() {
	}
	
	@Autowired
	private ChatService chatService;
	
	@Test
	void saveDataToVectorDatabase() {
		System.out.println("Savind data to MariaDB");
		this.chatService.saveData(Helper.getData());
	}
	

}
