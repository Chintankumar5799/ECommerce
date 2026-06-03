package com.ecommerce.demo.payment.controller;

import com.ecommerce.demo.auth.dto.CustomUserPrincipal;
import com.ecommerce.demo.order.controller.OrderController;
import com.ecommerce.demo.order.service.OrderService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
	public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> data,
			@AuthenticationPrincipal CustomUserPrincipal principal)
			throws StripeException {

		try {
			Long amount = Long.parseLong(data.get("amount").toString());
			log.info("Get Payment intent for {} ", data.getClass().getName());
			PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
					.setAmount(amount)
					.setCurrency("usd")
					.build();

			PaymentIntent intent = PaymentIntent.create(params);

			log.info("Payment intent created with {} ", intent.getCustomer());
			if ("succeeded".equals(intent.getStatus())) {
				Long userId = principal.getUserId();
				// orderService.createOrderFromCart(userId, intent.getId());

			}

			Map<String, Object> response = new HashMap<>();
			response.put("clientSecret", intent.getClientSecret());
			response.put("amount", amount);
			return ResponseEntity.ok(response);

		} catch (StripeException e) {
			log.error("StripeException: {}", e.getMessage());
			return ResponseEntity.status(502).body(Map.of("error",
					"Payment service temporily unavilable."));
		}
	}

	@PostMapping("/pay")
	public ResponseEntity<Map<String, Object>> payWithCard(@AuthenticationPrincipal CustomUserPrincipal principal,
			@RequestBody Map<String, Object> data)
			throws StripeException {

		try {
			if (!data.containsKey("paymentMethodId") || !data.containsKey("customerId")) {
				log.warn("Payment request missing required fields.");

				return ResponseEntity.badRequest().body(Map.of("error", "paymentMethodId and customerId are required"));
			}

			String paymentMethodId = data.get("paymentMethodId").toString();
			Long amount = Long.parseLong(data.get("amount").toString());
			String customerId = data.get("customerId").toString();

			log.info("Pay amount");
			// Create PaymentIntent and confirm immediately
			PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
					.setAmount(amount)
					.setCurrency("usd")
					.setPaymentMethod(paymentMethodId)
					.setCustomer(customerId)
					.setConfirm(true) // confirms immediately
					.setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
							.setEnabled(true)
							.setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
							.build())
					.build();

			PaymentIntent paymentIntent = PaymentIntent.create(params);
			log.info("Payment confirmed successfully for userId: {}", principal.getUserId());

			return ResponseEntity.ok(Map.of("status", paymentIntent.getStatus()));
		}

		catch (StripeException e) {
			log.error("Payment failed {} ", e.getMessage());
			return ResponseEntity.status(502).body(Map.of("error",
					"Payment service temporarily unavailable." + e.getMessage()));

		}

		catch (Exception e) {
			log.error("Unexpected error during Payment {}", e);
			return ResponseEntity.status(500).body(Map.of("error",
					"Service temporarily unavailable." + e.getMessage()));

		}

		// Map<String, Object> response = new HashMap<>();
		// response.put("status", paymentIntent.getStatus());
		// response.put("id", paymentIntent.getId());

		// return ResponseEntity.ok(response);
	}
}