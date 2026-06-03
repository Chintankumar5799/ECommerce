package com.ecommerce.demo.order.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.demo.auth.dto.CustomUserPrincipal;
import com.ecommerce.demo.order.dto.OrderItemResponse;
import com.ecommerce.demo.order.dto.OrderResponse;
import com.ecommerce.demo.order.service.OrderService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	private final static Logger log = LoggerFactory.getLogger(OrderController.class);

	@PostMapping("/confirm")
	public ResponseEntity<?> confirmFinalOrder(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam String paymentIntentId) {

		Long userId = principal.getUserId();
		log.info("Order is geeting confirmed for userId {}", userId);
		orderService.createOrderFromCart(userId, paymentIntentId);
		return ResponseEntity.ok("Order Officially Confirmed!");
	}

	@GetMapping("/orderHistory")
	public ResponseEntity<List<OrderResponse>> orderHistory(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Long userId = principal.getUserId();
		log.info("Order history for {}", userId);

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
		List<OrderResponse> orderList = orderService.orderHistory(userId, pageable);
		return ResponseEntity.status(HttpStatus.OK).body(orderList);
	}

	@GetMapping("/orderHistory/orderId")
	public ResponseEntity<List<OrderItemResponse>> orderHistoryByOrderId(
			@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestParam Long orderId) {

		Long userId = principal.getUserId();
		log.info("Order history for orderId {} ", orderId);
		List<OrderItemResponse> orderList = orderService.orderHistoryByOrderId(userId, orderId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderList);
	}

	@PutMapping("/orderStatus")
	public ResponseEntity<String> orderStatus(@RequestParam Long orderId, @RequestParam String orderStatus) {
		log.info("Change order status for orderId {} ", orderId);
		OrderResponse updatedStatus = orderService.orderStatus(orderId, orderStatus);
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body("Your Status has been updated to" + updatedStatus.getOrderStatus());
	}

	// @PostMapping("/newOrder")
	// public ResponseEntity<?> confirmFinalOrder() {
	// orderService.newOrder();
	// return null;
	// }

}
