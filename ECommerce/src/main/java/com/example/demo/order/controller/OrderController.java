package com.example.demo.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.order.dao.OrderItemResponse;
import com.example.demo.order.dao.OrderResponse;
import com.example.demo.order.entity.Order;
import com.example.demo.order.entity.OrderItem;
import com.example.demo.order.service.OrderService;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
	
	public OrderController(OrderService orderService){
		this.orderService = orderService;
	}

	@PostMapping("/confirm")
	public ResponseEntity<?> confirmFinalOrder(@RequestParam Long userId, @RequestParam String paymentIntentId) {
	   
		System.out.println("User ID is here..."+userId);
	    orderService.createOrderFromCart(userId, paymentIntentId);
	    
	    return ResponseEntity.ok("Order Officially Confirmed!");
	}
	
	@GetMapping("/orderHistory")
	public ResponseEntity<List<OrderResponse>> orderHistory(@RequestParam Long userId){
		List<com.example.demo.order.dao.OrderResponse> orderList=orderService.orderHistory(userId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderList);
	}
	
	@GetMapping("/orderHistory/orderId")
	public ResponseEntity<List<OrderItemResponse>> orderHistoryByOrderId(@RequestParam Long userId, @RequestParam Long orderId){
		List<com.example.demo.order.dao.OrderItemResponse> orderList=orderService.orderHistoryByOrderId(userId, orderId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderList);
	}
	
	
	
//	@PostMapping("/newOrder")
//	public ResponseEntity<?> confirmFinalOrder() {
//		orderService.newOrder();
//		return null;
//	}
	
}
