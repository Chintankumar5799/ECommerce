package com.example.demo.payment.controller;

import com.example.demo.order.controller.OrderController;
import com.example.demo.order.service.OrderService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

	private final OrderService orderService;

	PaymentController(OrderService orderService) {
		this.orderService = orderService;
	}

	private final static Logger log = LoggerFactory.getLogger(PaymentController.class);

	@PostMapping("/create-payment-intent")
	public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> data)
			throws StripeException {

		try {
			Long amount = Long.parseLong(data.get("amount").toString());
			log.info("Get Payment intent for" + data.getClass().getName());
			PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
					.setAmount(amount)
					.setCurrency("usd")
					.build();

			PaymentIntent intent = PaymentIntent.create(params);

			log.info("Payment intent created with" + intent.getCustomer());
			if ("succeeded".equals(intent.getStatus())) {
				Long userId = Long.parseLong(data.get("userId").toString());
				// orderService.createOrderFromCart(userId, intent.getId());

			}

			Map<String, Object> response = new HashMap<>();
			response.put("clientSecret", intent.getClientSecret());
			response.put("amount", amount);
			return ResponseEntity.ok(response);

		} catch (StripeException e) {
			log.error("StripeException: " + e.getMessage());
			return ResponseEntity.status(502).body(Map.of("error",
					"Payment service temporily unavilable."));
		}
	}

	@PostMapping("/pay")
	public ResponseEntity<Map<String, Object>> payWithCard(@RequestBody Map<String, Object> data)
			throws StripeException {

		String paymentMethodId = data.get("paymentMethodId").toString();
		Long amount = Long.parseLong(data.get("amount").toString());
		log.info("Pay amount");
		// Create PaymentIntent and confirm immediately
		PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
				.setAmount(amount)
				.setCurrency("usd")
				.setPaymentMethod(paymentMethodId)
				.setConfirm(true) // confirms immediately
				.build();

		PaymentIntent paymentIntent = PaymentIntent.create(params);

		Map<String, Object> response = new HashMap<>();
		response.put("status", paymentIntent.getStatus());
		response.put("id", paymentIntent.getId());

		return ResponseEntity.ok(response);
	}
}
