package com.example.demo.order.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.helpdesk.TicketService;
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
	
	
	private final static Logger log=LoggerFactory.getLogger(OrderController.class);

	@PostMapping("/confirm")
	public ResponseEntity<?> confirmFinalOrder(@RequestParam Long userId, @RequestParam String paymentIntentId) {
	   
		log.info("Order is geeting confirmed for userId "+userId);
	    orderService.createOrderFromCart(userId, paymentIntentId);
	    return ResponseEntity.ok("Order Officially Confirmed!");
	}
	
	@GetMapping("/orderHistory")
	public ResponseEntity<List<OrderResponse>> orderHistory(@RequestParam Long userId){
		log.info("Order history for "+userId);
		List<OrderResponse> orderList=orderService.orderHistory(userId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderList);
	}
	
	@GetMapping("/orderHistory/orderId")
	public ResponseEntity<List<OrderItemResponse>> orderHistoryByOrderId(@RequestParam Long userId, @RequestParam Long orderId){
		log.info("Order history for orderId "+orderId);
		List<OrderItemResponse> orderList=orderService.orderHistoryByOrderId(userId, orderId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderList);
	}
	
	
	@PutMapping("/orderStatus")
	public ResponseEntity<String> orderStatus(@RequestParam Long orderId,@RequestParam String orderStatus) {
		log.info("Change order status for orderId "+orderId);
		OrderResponse updatedStatus=orderService.orderStatus(orderId,orderStatus);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Your Status has been updated to"+updatedStatus.getOrderStatus());
	}
	
	
//	@PostMapping("/newOrder")
//	public ResponseEntity<?> confirmFinalOrder() {
//		orderService.newOrder();
//		return null;
//	}
	
}
