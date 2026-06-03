package com.ecommerce.demo.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.demo.auth.entity.User;
import com.ecommerce.demo.auth.repository.UserRepository;
import com.ecommerce.demo.cart.dto.CartStatus;
import com.ecommerce.demo.cart.entity.Cart;
import com.ecommerce.demo.cart.repository.CartRepository;
import com.ecommerce.demo.category.entity.Product;
import com.ecommerce.demo.category.entity.ProductVariants;
import com.ecommerce.demo.category.repository.ProductRepository;
import com.ecommerce.demo.exception.ResourceNotFoundException;
import com.ecommerce.demo.order.dto.OrderItemResponse;
import com.ecommerce.demo.order.dto.OrderResponse;
import com.ecommerce.demo.order.entity.Order;
import com.ecommerce.demo.order.entity.OrderItem;
import com.ecommerce.demo.order.repository.OrderItemRepository;
import com.ecommerce.demo.order.repository.OrderRepository;

@Service
public class OrderService {

	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final UserRepository userRepository;

	private final static Logger log = LoggerFactory.getLogger(OrderService.class);

	public OrderService(CartRepository cartRepository, ProductRepository productRepository,
			OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserRepository userRepository) {
		this.cartRepository = cartRepository;
		this.productRepository = productRepository;
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.userRepository = userRepository;
	}

	@org.springframework.transaction.annotation.Transactional
	public void createOrderFromCart(Long userId, String id) {
		List<Cart> allItems = cartRepository.findAllByUserId(userId);
		Long subTotal = 0L;
		Long baseSubTotal = 0L;

		log.info("create order from cart with payment intent id: {} ", id);
		// ONLY get items that are actually in the Cart (ignore Wishlist!)
		List<Cart> cartItems = new ArrayList<>();
		for (Cart c : allItems) {
			if (CartStatus.CART.equals(c.getStatus())) {
				cartItems.add(c);
			}
		}

		// 2. Prevent creating orders for empty carts
		if (cartItems.isEmpty()) {
			return; // Nothing to buy!
		}

		Optional<User> user = userRepository.findById(userId);
		// 3. Create and save the parent Order first (so order has an ID for order
		// items)
		Order order = new Order();
		order.setUserId(userId);
		order.setTransactionId(id);
		order.setOrderStatus("PLACED"); // PLACED, SHIPPED, DELIVERED
		order.setPaymentMethod("CARD"); // CARD, UPI, COD
		order.setPaymentStatus("PAID"); // PAID, FAILED, PENDING
		if (user.isPresent() && user.get().getAddress() != null) {
			order.setAddress(user.get().getAddress().toString());
		} else {
			order.setAddress("Customer Address");
		}
		order.setTotalAmount(0L);
		order.setTaxAmount(0L);
		order.setDiscountAmount(0L);
		order.setShippingCharge(0L);
		order = orderRepository.save(order);
		log.info("Order details is saved in Database.");

		for (Cart c : cartItems) {
			OrderItem orderItem = new OrderItem();
			orderItem.setProductVariantName(c.getVariant().getProduct().getProductName());

			Product product = new Product();
			ProductVariants variant = new ProductVariants();

			product.setId(c.getVariant().getProduct().getId());
			variant.setVariantId(c.getVariant().getVariantId());

			orderItem.setProduct(product);
			orderItem.setVariant(variant);
			orderItem.setQuantity(c.getQuantity());
			orderItem.setOrder(order);

			Long unitPrice = c.getVariant().getOfferPrice();
			if (unitPrice == null || unitPrice == 0) {
				unitPrice = c.getVariant().getPrice();
			}

			Long totalItemPrice = c.getQuantity() * unitPrice;
			orderItem.setTotalPrice(totalItemPrice);
			orderItem.setPrice(unitPrice);

			subTotal += totalItemPrice;

			Long originalPrice = c.getVariant().getPrice();
			if (originalPrice == null) {
				originalPrice = unitPrice;
			}
			baseSubTotal += c.getQuantity() * originalPrice;

			orderItemRepository.save(orderItem);

		}
		// if (cartItems.isEmpty()) {
		// return; // Nothing to buy!
		// }

		// Optional<User> user = userRepository.findById(userId);

		// Order order = new Order();
		// order.setUserId(userId);

		// order.setTransactionId(id);
		// order.setOrderStatus("PLACED"); // PLACED, SHIPPED, DELIVERED
		// order.setPaymentMethod("CARD"); // CARD, UPI, COD
		// order.setPaymentStatus("PAID"); // PAID, FAILED, PENDING

		// order.setAddress(user.get().getAddress().toString()); // Usually comes from
		// Checkout Form
		// order.setTotalAmount(0L);
		// order.setTaxAmount(0L);
		// order.setDiscountAmount(0L);
		// order.setShippingCharge(0L);

		// order = orderRepository.save(order);
		// log.info("Order details is saved in Database.");
		// Long grandTotalPrice = 0L;

		// for (Cart c : cartItems) {

		// OrderItem orderItem = new OrderItem();

		// orderItem.setProductVariantName(c.getVariant().getProduct().getProductName());
		// // c.getVariant().getVariantAttributes();

		// Product p = new Product();
		// ProductVariants pv = new ProductVariants();

		// p.setId(c.getVariant().getProduct().getId());
		// pv.setVariantId(c.getVariant().getVariantId());

		// orderItem.setVariant(pv);
		// orderItem.setProduct(p);
		// orderItem.setQuantity(c.getQuantity());
		// orderItem.setOrder(order);

		// Long totalItemPrice = c.getQuantity() * c.getVariant().getOfferPrice();
		// orderItem.setTotalPrice(totalItemPrice);
		// orderItem.setPrice(c.getVariant().getOfferPrice());

		// grandTotalPrice += totalItemPrice;
		// orderItemRepository.save(orderItem);

		// }

		Long discountAmount = baseSubTotal - subTotal;
		Long shippingCharge = (long) (subTotal * 0.01);
		Long taxAmount = (long) (subTotal * 0.03);
		Long grandTotal = subTotal + shippingCharge + taxAmount;

		order.setTotalAmount(grandTotal);
		log.info("Total price {} , Shipping charge {}", order.getTotalAmount(), order.getTaxAmount());
		order.setDiscountAmount(discountAmount); // actually it is diff of all product to actual to orderprice
		order.setShippingCharge(shippingCharge);
		order.setTaxAmount(taxAmount);
		orderRepository.save(order);

		cartRepository.deleteAll(cartItems);

	}

	// public void newOrder() {
	//
	// Order order=new Order();
	// order.setUserId(30L);
	// order.setAddress("sjknfjsncs");
	// order.setDiscountAmount(30L);
	//// order.setItems(new ArrayList<>(List.of("ANC","DFF")));
	// order.setPaymentMethod("CARD");
	// order.setPaymentStatus("PAID");
	// order.setOrderStatus("PLACED");
	// order.setShippingCharge(null);
	// order.setTaxAmount(30L);
	// order.setTotalAmount(30000L);
	// order.setTransactionId("EIDJa0912485nakjdn");
	//
	// orderRepository.save(order);
	// }

	@Transactional(readOnly = true)
	public List<OrderResponse> orderHistory(Long userId, org.springframework.data.domain.Pageable pageable) {
		List<Order> orderList = orderRepository.findByUserId(userId, pageable).getContent();
		List<OrderResponse> orderResponseList = new ArrayList<>();

		log.info("Order id is found for {} ", orderList);

		for (Order order : orderList) {
			OrderResponse orderResponse = new OrderResponse();
			orderResponse.setAddress(order.getAddress());
			orderResponse.setOrderStatus(order.getOrderStatus());
			orderResponse.setPaymentMethod(order.getPaymentMethod());
			orderResponse.setPaymentStatus(order.getPaymentStatus());
			orderResponse.setTotalAmount(order.getTotalAmount());
			orderResponse.setTransactionId(order.getTransactionId());

			orderResponse.setDiscountAmount(order.getDiscountAmount());
			orderResponse.setShippingCharge(order.getShippingCharge());
			orderResponse.setTaxAmount(order.getTaxAmount());

			orderResponseList.add(orderResponse);

		}

		return orderResponseList;
	}

	public List<OrderItemResponse> orderHistoryByOrderId(Long userId, Long orderId) {
		List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
		List<OrderItemResponse> orderItemResponseList = new ArrayList<>();
		log.info("Got details for orderId {}", orderId);

		for (OrderItem orderItem : orderItems) {
			OrderItemResponse orderResponse = new OrderItemResponse();
			orderResponse.setId(orderItem.getId());
			orderResponse.setPrice(orderItem.getPrice());
			orderResponse.setProductId(orderItem.getProduct().getId());
			orderResponse.setProductVariantName(orderItem.getProductVariantName());
			orderResponse.setQuantity(orderItem.getQuantity());
			orderResponse.setTotalPrice(orderItem.getTotalPrice());
			orderResponse.setVariantId(orderItem.getVariant().getVariantId());

			orderItemResponseList.add(orderResponse);
		}
		return orderItemResponseList;
	}

	public OrderResponse orderStatus(Long orderId, String orderStatus) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("No order found"));

		order.setOrderStatus(orderStatus);
		orderRepository.save(order);
		log.info("Order status is saved in {}", orderStatus);

		OrderResponse orderResponse = new OrderResponse();
		orderResponse.setOrderStatus(order.getOrderStatus());

		return orderResponse;
	}

}
