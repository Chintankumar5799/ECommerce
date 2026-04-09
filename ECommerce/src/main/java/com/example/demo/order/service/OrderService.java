package com.example.demo.order.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.cart.entity.Cart;
import com.example.demo.cart.repository.CartRepository;
import com.example.demo.category.entity.Product;
import com.example.demo.category.entity.ProductVariants;
import com.example.demo.category.repository.ProductRepository;
import com.example.demo.order.dao.OrderItemResponse;
import com.example.demo.order.dao.OrderResponse;
import com.example.demo.order.entity.Order;
import com.example.demo.order.entity.OrderItem;
import com.example.demo.order.repository.OrderItemRepository;
import com.example.demo.order.repository.OrderRepository;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Service
public class OrderService {
	
	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	
	
	public OrderService(CartRepository cartRepository, ProductRepository productRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
		this.cartRepository=cartRepository;
		this.productRepository=productRepository;
		this.orderRepository=orderRepository;
		this.orderItemRepository=orderItemRepository;
	}

	@org.springframework.transaction.annotation.Transactional
	public void createOrderFromCart(Long userId, String id) {
		List<Cart> allItems = cartRepository.findByUserId(userId);
		
		// ONLY get items that are actually in the Cart (ignore Wishlist!)
		List<Cart> cartItems = new ArrayList<>();
		for(Cart c : allItems) {
			if(com.example.demo.cart.dao.CartStatus.CART.equals(c.getStatus())) {
				cartItems.add(c);
			}
		}
		
		if(cartItems.isEmpty()) {
			return; // Nothing to buy!
		}
		
		Order order=new Order();
		order.setUserId(userId);
		
		System.out.println(order.getUserId());
		order.setTransactionId(id);
		order.setOrderStatus("PLACED");   // PLACED, SHIPPED, DELIVERED
		order.setPaymentMethod("CARD");	  // CARD, UPI, COD
		order.setPaymentStatus("PAID");   // PAID, FAILED, PENDING
		
		order.setAddress("Customer Address"); // Usually comes from Checkout Form
		order.setTotalAmount(0L);
		order.setTaxAmount(0L);
		order.setDiscountAmount(0L);
		order.setShippingCharge(0L);
		
		order = orderRepository.save(order);
		Long grandTotalPrice=0L;
		
		for(Cart c:cartItems) {
			
			
			OrderItem orderItem=new OrderItem();
			
			orderItem.setProductVariantName(c.getVariant().getProduct().getProductName());
//			c.getVariant().getVariantAttributes();
			
			Product p=new Product();
			ProductVariants pv=new ProductVariants();
			
			p.setId(c.getVariant().getProduct().getId());
            pv.setVariantId(c.getVariant().getVariantId());
            
			orderItem.setVariant(pv);
			orderItem.setProduct(p);
			orderItem.setQuantity(c.getQuantity());
			orderItem.setOrder(order);
			
			Long totalItemPrice=c.getQuantity() * c.getVariant().getOfferPrice();
		    orderItem.setTotalPrice(totalItemPrice);
		    orderItem.setPrice(c.getVariant().getOfferPrice());
		    
		    grandTotalPrice+=totalItemPrice;
		    orderItemRepository.save(orderItem);

		}
		
		order.setTotalAmount(grandTotalPrice);
		System.out.println("total price "+order.getTotalAmount()+ " "+"Shipping charge"+ order.getTaxAmount());
		order.setDiscountAmount(grandTotalPrice); //actually it is diff of all product to actual to orderprice
		order.setShippingCharge((long)(grandTotalPrice*0.01));
		order.setTaxAmount((long)(grandTotalPrice*0.03));
		orderRepository.save(order);
		
		cartRepository.deleteAll(cartItems);
		
	}

//	public void newOrder() {
//		
//		Order order=new Order();
//		order.setUserId(30L);
//		order.setAddress("sjknfjsncs");
//		order.setDiscountAmount(30L);
////		order.setItems(new ArrayList<>(List.of("ANC","DFF")));
//		order.setPaymentMethod("CARD");
//		order.setPaymentStatus("PAID");
//		order.setOrderStatus("PLACED");
//		order.setShippingCharge(null);
//		order.setTaxAmount(30L);
//		order.setTotalAmount(30000L);
//		order.setTransactionId("EIDJa0912485nakjdn");
//		
//		orderRepository.save(order);
//	}

	public List<OrderResponse> orderHistory(Long userId) {
		List<Order> orderList=orderRepository.findByUserId(userId);
		List<OrderResponse> orderResponseList=new ArrayList<>();
		
		for(Order order:orderList) {
			OrderResponse orderResponse=new OrderResponse();
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
		List<OrderItem> orderItems=orderItemRepository.findByOrderId(orderId);
		List<OrderItemResponse> orderItemResponseList=new ArrayList<>();
		
		for(OrderItem orderItem:orderItems) {
			OrderItemResponse orderResponse=new OrderItemResponse();
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

}
