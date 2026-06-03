# Backend Bug Solutions Log

This document tracks issues encountered during development and their confirmed solutions.

---

## 1. Localhost Database Socket Timeout (IPv6 vs IPv4)

**Error:**
```text
java.sql.SQLTimeoutException: Socket timeout when connecting to localhost. Read timed out
```
(Specifically occurred when HikariPool attempted to connect to the MariaDB vector store and PostgreSQL.)

**Cause:**
On modern Windows systems, Java often resolves the hostname `localhost` to the IPv6 loopback address (`::1`). However, local database installations or Docker Desktop containers often bind exclusively to the IPv4 loopback address (`127.0.0.1`). 
Because nothing was actively listening on IPv6 to reject the connection immediately, Java's connection request hung in a "black hole" until the socket timer ran out.

**Solution:**
Open `application-local.properties` (or any active profile properties) and replace `localhost` with the explicit IPv4 IP `127.0.0.1` in all JDBC connection URLs.

*Before:*
```properties
spring.datasource.postgres.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.mariadb.url=jdbc:mariadb://localhost:3306/springai
```

*After:*
```properties
spring.datasource.postgres.url=jdbc:postgresql://127.0.0.1:5432/ecommerce
spring.datasource.mariadb.url=jdbc:mariadb://127.0.0.1:3306/springai
```

---

## 2. CORS Wide Open Vulnerability (Allowing any Origin)

**Vulnerability:**
The application had CORS configured with a wildcard `"*"` alongside `allowCredentials(true)`. This is a severe security vulnerability that allows any website on the internet to make authenticated requests to the API.

**Cause:**
`configuration.setAllowedOriginPatterns(java.util.List.of("*"));` was hardcoded in `SecurityConfig.java`.

**Solution:**
Create a configuration property in `application.properties` that defines exactly which domains are allowed (the frontend URLs). Inject this into `SecurityConfig.java` and use it to configure CORS.

*In `application.properties` (for local development):*
```properties
# Allowed Cors Origins
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

*In `SecurityConfig.java`:*
```java
@Value("${app.cors.allowed-origins}")
private List<String> allowedOrigins;

// ... inside corsConfigurationSource() ...
configuration.setAllowedOriginPatterns(allowedOrigins);
```

---

## 3. Hibernate ddl-auto Modes Explained (Data Loss Risk)

**Concept:** 
The `spring.jpa.hibernate.ddl-auto` property tells Hibernate how to manage your database schema based on your Java entities.

**Modes:**
* `create` / `create-drop`: **(Extremely Dangerous)** Drops all tables and recreates them every time the application starts. **You will lose 100% of your data.** Only use for automated tests.
* `update`: **(Risky in Prod)** Compares Java entities to the database tables and runs `ALTER TABLE` to add missing columns. Can cause unexpected data loss or table locks in a live production system. Fine for local development.
* `validate`: **(Best Practice for Prod)** Does not modify the database. It only checks if your Java entities perfectly match the existing tables. If they don't, the app crashes to protect data integrity.

**Best Practice:**
Always use `validate` (or `none`) in production profiles (`application-prod.properties`). Since the project uses **Flyway** for database migrations, Flyway should be the only tool modifying tables, while Hibernate should only `validate` them. 

*(Note: If you write the same property key multiple times in a `.properties` file, Spring Boot just reads the file top-to-bottom and the last entry completely overwrites the earlier ones).*

---

## 4. IDOR Vulnerability in Business Controllers (userId spoofing)

**Problem (H1 Vulnerability):**
The application accepted `userId` from the frontend (via `@RequestParam Long userId` or in the `@RequestBody`) for sensitive endpoints like `addToCart` and `createPaymentIntent`. This creates an Insecure Direct Object Reference (IDOR) vulnerability, where a malicious logged-in user can change the `userId` in the HTTP request to access or modify another user's cart or orders.

**Inefficient Solution (Database Query per Request):**
The naive fix is to remove `userId` from the request parameters and instead look up the user by email (`userRepository.findByEmail(principal.getName())`) inside every business controller (like `CartController`). 
*Why it's bad:* This forces a database query on every single API request, which severely degrades performance on high-traffic endpoints.

**Optimal Solution (JWT Custom Claims):**
Instead of querying the database on every request, we look up the `userId` **exactly once** during the login process and seal it inside the JWT token as a custom claim.

*Step 1: AuthController (Login / Refresh)*
When the user logs in, query the database, get the `userId`, and pass it to the JWT generator.
```java
User user = userRepository.findByEmail(request.getEmail());
String accessToken = jwtUtil.generateToken(userDetails, user.getId());
```

*Step 2: JwtUtil (Token Generation)*
Embed the `userId` into the token payload so it travels securely with the client.
```java
return Jwts.builder()
    // ...
    .claim("userId", userId)
    .compact();
```

*Step 3: Business Controllers (Cart, Order)*
The controllers never hit the database to find the user. They simply ask Spring Security for the custom principal object that was built by reading the incoming JWT token.
```java
@PostMapping("/addToCart")
public ResponseEntity<?> addToCart(@AuthenticationPrincipal CustomUserPrincipal principal, ...) {
    Long userId = principal.getUserId(); // Extracted instantly from memory!
}
```

**Rule of Thumb:**
* **Login/OAuth Endpoints:** It is necessary and correct to query the database here to authenticate the user and build their token.
* **Business Endpoints (Cart, Orders):** Do NOT query the database just to figure out "who the user is". Rely entirely on the JWT token contents (extracted via `@AuthenticationPrincipal CustomUserPrincipal`).


## 5. Tight coupling with @Configuration

A `@Configuration` bean should **never** be injected into a controller. This creates tight coupling and is never used in the code.

**Fix:** Remove this field entirely.

## 6. Dependency Injection Best Practices (Field vs Constructor)

**Problem:** 
The codebase previously mixed `@Autowired` field injection with constructor injection (e.g., in `AuthController` and `CategoryService`). 

**Solution:**
Spring recommends **constructor injection exclusively** because:
1. It ensures the class cannot be instantiated without its dependencies (fail-fast).
2. It makes dependencies `final`, preventing them from being changed at runtime.
3. It makes unit testing easier (you can instantiate the class without a Spring context).

*Fix applied:* Removed all `@Autowired` fields and injected them via `private final` constructor parameters.

## 7. Strict Layered Architecture (Controller -> Service -> Repository)

**Problem:**
`UserRepository` was directly injected into `AuthController`.

**Solution:**
Controllers should only handle HTTP requests and delegate database interactions to the Service layer. Controllers should **never** talk to repositories directly. 

*Fix applied:* Moved the `findByEmail` logic into `UserService` and had the controller call `userService.findByEmail(email)`.

## 8. Proper Application Logging (No System.out or printStackTrace)

**Problem:**
The application used `System.out.println()` for debugging and `e.printStackTrace()` for error handling. 

**Solution:**
* `System.out.println` bypasses the application's logging framework (Logback), meaning these logs cannot be formatted, routed to files, or filtered by severity (INFO/DEBUG/ERROR). 
* `e.printStackTrace()` writes directly to standard error, bypassing structured logging and risking loss of stack traces in Docker/cloud environments.

*Fix applied:* Replaced all instances with SLF4J's `log.info()` and `log.error()`. Used parameterized logging (e.g., `log.info("User {}", userId)`) instead of string concatenation to improve performance.

## 9. Handling External API Calls (Stripe Payment Integration)

**Problem (H11):**
The `/pay` endpoint in `PaymentController` lacked basic error handling. If `paymentMethodId` was missing, it threw a `NullPointerException`. If the Stripe API failed, it threw a raw `StripeException`, causing a 500 error without a meaningful response to the frontend.

**Solution:**
When calling an external service (like Stripe, AWS, or an AI API), you must:
1. Validate all inputs before making the call.
2. Catch specific exceptions (like `StripeException`) to handle known API failures.
3. Catch generic `Exception` as a fallback so the server never crashes unexpectedly.

**Why catch two different exceptions?**
*   **`catch (StripeException e)`**: This specifically catches errors thrown by Stripe (e.g., card declined, API key invalid). We return a `502 Bad Gateway` (meaning "the upstream service we rely on failed") and show the specific Stripe error message.
*   **`catch (Exception e)`**: This is the "safety net". If our own code throws a `NullPointerException` or `NumberFormatException` (e.g., `Long.parseLong` fails), we catch it here. We log the full stack trace (`log.error("Unexpected error", e)`) and return a `500 Internal Server Error`.

**What is `Map.of()` and why use it?**
Instead of doing:
```java
Map<String, Object> response = new HashMap<>();
response.put("status", intent.getStatus());
return ResponseEntity.ok(response);
```
We use:
```java
return ResponseEntity.ok(Map.of("status", intent.getStatus()));
```
*   **How it works:** `Map.of(key1, value1, key2, value2)` is a convenience method introduced in Java 9. It creates an *immutable* map in a single line of code.
*   **Why use it:** It makes code much shorter, cleaner, and more readable. It prevents you from having to instantiate a `HashMap` and call `.put()` multiple times just to return a simple JSON object.
*   **When to use it:** Use it whenever you need to return a simple JSON object with 1 to 10 keys and you don't need to change the map later. *(Note: `Map.of` does not allow `null` keys or values).*

**The Complete, Secure Method:**
```java
	@PostMapping("/pay")
	public ResponseEntity<Map<String, Object>> payWithCard(
			@AuthenticationPrincipal CustomUserPrincipal principal, 
			@RequestBody Map<String, Object> data) {

		try {
			// 1. Validate inputs before doing anything
			if (!data.containsKey("paymentMethodId") || !data.containsKey("customerId") || !data.containsKey("amount")) {
				log.warn("Payment request missing required fields.");
				return ResponseEntity.badRequest().body(Map.of("error", "paymentMethodId, customerId, and amount are required"));
			}

			String paymentMethodId = data.get("paymentMethodId").toString();
			String customerId = data.get("customerId").toString();
			Long amount = Long.parseLong(data.get("amount").toString());

			log.info("Processing payment for userId: {}", principal.getUserId());

			// 2. Call Stripe API
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

			// 3. Return success using Map.of
			return ResponseEntity.ok(Map.of(
				"status", paymentIntent.getStatus(),
				"id", paymentIntent.getId()
			));

		} catch (StripeException e) {
			log.error("Payment failed: {}", e.getMessage());
			return ResponseEntity.status(502).body(Map.of("error", "Payment service temporarily unavailable: " + e.getMessage()));
			
		} catch (Exception e) {
			log.error("Unexpected error during Payment: ", e);
			return ResponseEntity.status(500).body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
		}
	}
```

## 10. Returning DTOs Instead of Entities (N+1 and Caching Risks)

**Problem (H12):**
The `getAllProducts()` method in `ProductService` was returning a `List<Product>` (the actual JPA entity) directly to the controller, and it was cached using `@Cacheable("products")`. 

**Why is returning entities bad?**
1. **Serialization Issues:** Jackson (the library that converts Java to JSON) tries to serialize every field. If `Product` has relations (like `@ManyToOne User seller`), Jackson might trigger a database query (N+1 problem) or get stuck in an infinite loop (Circular Reference).
2. **Caching Entities is Dangerous:** `@Cacheable` stores the exact object in memory. If you cache a JPA Entity, it becomes "detached" from the database session. If another part of the code pulls that entity from the cache and tries to access a lazy-loaded field (like `.getSeller().getName()`), it will crash with a `LazyInitializationException`.

**Solution:**
Always map JPA Entities to Data Transfer Objects (DTOs) *before* returning them from the service layer, especially when caching. 

*Fix applied:* 
1. Mapped the `List<Product>` into a `List<ProductResponse>`.
2. Returned the DTO list from `getAllProducts()`.
3. Updated the `ProductController` endpoint `/allProducts` to return `ResponseEntity<List<ProductResponse>>`.

---

## 11. CartService.removeItem Deleting by Wrong ID

**Error:**
`removeItem()` was supposed to remove a specific item from a user's cart, but it was calling `cartRepository.deleteById(userId)` — which treats `userId` as the cart row's primary key. It also completely ignored the `variantId` parameter.

```java
// BUG: deletes the Cart row whose primary key == userId, NOT the user's cart item
public String removeItem(Long userId, Long variantId) {
    cartRepository.deleteById(userId);
    return "Item removed Successfully";
}
```

**Cause:**
`deleteById()` is a built-in Spring Data method that deletes by the entity's `@Id` (the Cart table's primary key). Passing `userId` into it doesn't filter by the user — it deletes whichever cart row happens to have that primary key value. The `variantId` parameter was accepted but never used.

**Solution:**
Create a proper derived delete method in `CartRepository` that deletes by both `userId` and `variantId`, and call that instead.

*Fix applied:*
1. Added `deleteByUserIdAndVariantVariantId(Long userId, Long variantId)` to `CartRepository`.
2. Updated `CartService.removeItem()` to call the new method.
3. Added `@Transactional` on the service method (required for Spring Data derived delete queries).

---

## 12. Spring Data Derived Query — `No property 'id' found for type 'ProductVariants'`

**Error:**
```text
Could not create query for method public abstract void
com.example.demo.cart.repository.CartRepository.deleteByUserIdAndVariantId(Long, Long);
No property 'id' found for type 'ProductVariants'; Traversed path: Cart.variant
```

**Cause:**
When Spring Data parses `deleteByUserIdAndVariantId`, it breaks the method name into property paths:
- `User` → `Id` → resolves to `Cart.user.id` ✅
- `Variant` → `Id` → tries to resolve `Cart.variant.id` ❌

But `ProductVariants` does **not** have a field called `id`. Its primary key field is named `variantId`. So Spring Data fails during startup because it can't find `Cart.variant.id`.

Note: The existing `findCartByUserIdAndVariantId` method worked fine because it used a custom `@Query` annotation with explicit JPQL, bypassing Spring Data's property name resolution entirely.

**Solution:**
Use the full property path `VariantVariantId` in the method name so Spring Data correctly resolves it as `Cart.variant.variantId`.

*Before:*
```java
void deleteByUserIdAndVariantId(Long userId, Long variantId);
```

*After:*
```java
void deleteByUserIdAndVariantVariantId(Long userId, Long variantId);
```

**Key Takeaway:** For Spring Data derived queries, the method name must exactly follow the entity's property chain. If a related entity's primary key is not named `id` (e.g., `variantId` instead of `id`), you must spell out the full field name in the method name: `VariantVariantId` = navigate to `variant` field → then its `variantId` property.

---

## 13. CartService.addToCart Duplicate Check Missing (M8)

**Error:**
Adding the same product variant multiple times created duplicate entries in the user's cart instead of incrementing the quantity of the existing entry.

**Cause:**
In the original `addToCart()` method, a new `Cart` entity was instantiated and saved to the database on every execution. There was no check to see if an entry already existed for the given `userId` and `variantId`.

**Solution:**
Use the repository's `findCartByUserIdAndVariantId` method to check if a record already exists for the given user and variant.
- If it exists and has a status of `CART`, increment the quantity.
- If it exists but has a status of `WISHLIST`, update the status to `CART` and set the quantity.
- If it doesn't exist, instantiate a new `Cart` entity.

*Before:*
```java
public CartResponse addToCart(Long userId, Long variantId, int quantity) {
    Cart cart = new Cart();

    User user = new User();
    user.setId(userId);

    ProductVariants productVariants = new ProductVariants();
    productVariants.setVariantId(variantId);

    cart.setUser(user);
    cart.setVariant(productVariants);
    cart.setQuantity(quantity);
    cart.setStatus(CartStatus.CART);
    
    Cart savedProduct = cartRepository.save(cart);
    ...
}
```

*After:*
```java
public CartResponse addToCart(Long userId, Long variantId, int quantity) {
    Cart cart = new Cart();
    Optional<Cart> existingProduct = cartRepository.findCartByUserIdAndVariantId(userId, variantId);
    if (existingProduct.isPresent()) {
        cart = existingProduct.get();

        if (cart.getStatus() == CartStatus.CART) {
            cart.setQuantity(cart.getQuantity() + quantity);
        } else {
            cart.setStatus(CartStatus.CART);
            cart.setQuantity(quantity);
        }
    } else {
        User user = new User();
        user.setId(userId);

        ProductVariants productVariants = new ProductVariants();
        productVariants.setVariantId(variantId);

        cart.setUser(user);
        cart.setVariant(productVariants);
        cart.setQuantity(quantity);
        cart.setStatus(CartStatus.CART);
    }

    Cart savedProduct = cartRepository.save(cart);
    ...
}
```

---

## 14. E-Commerce Checkout Pricing, Tax, Shipping, & Double-Discounting Bugs (M9)

**Error:**
The order creation logic (`OrderService.createOrderFromCart()`) had multiple mathematical and runtime flaws:
1. **Double Discounting:** The final payable `totalAmount` had the discount subtracted from the subtotal *again*, even though the subtotal was already calculated using the promotional `offerPrice`.
2. **Hardcoded Discount Tracker:** The discount was set equal to the entire order subtotal, recording a 100% discount.
3. **Quantity Ignored for Base Price:** When calculating the original price of the order before the discount (`baseSubTotal`), it added the variant's original price exactly once, ignoring the number of items purchased (quantity).
4. **Undeclared Variable Compilation Error:** The parent `Order` object declaration was commented out, causing compilation to fail because order items were trying to link to it.
5. **No Null-Safety for Offer Price:** There was no fallback if a product had no promotional discount (`offerPrice = null`), which would crash checkout with a `NullPointerException`.
6. **No Empty Cart Check:** If a user hit the checkout endpoint with an empty cart, it would save an empty order in the database.

---

### **How to Understand the E-Commerce Pricing Math:**
In any e-commerce platform, there are two subtotal concepts:
1. **Base Subtotal (Original Value):** What the items normally cost before any sales.
   $$\text{Base Subtotal} = \sum (\text{Quantity} \times \text{Original Price})$$
2. **Actual Subtotal (Discounted Value):** What the user is actually paying for the items (uses `offerPrice` if on sale, falls back to `original price` if not).
   $$\text{Actual Subtotal} = \sum (\text{Quantity} \times \text{Purchase Price})$$
3. **Discount Amount (Money Saved):** The difference between the original and actual values.
   $$\text{Discount Amount} = \text{Base Subtotal} - \text{Actual Subtotal}$$
4. **Extra Charges:** Shipping (1%) and Tax (3%) calculated from the Actual Subtotal.
5. **Grand Total (Billed Amount):** The final amount the credit card is charged.
   $$\text{Grand Total} = \text{Actual Subtotal} + \text{Shipping} + \text{Tax}$$

*Note: Since the Actual Subtotal already has the discount applied at the item level, you must **NEVER** subtract the discount a second time at the end. Doing so results in double discounting.*

---

### **Code Comparison:**

#### *Before:*
```java
public void createOrderFromCart(Long userId, String id) {
    // 1. No empty cart guard
    // 2. Order parent declaration commented out / missing
    
    for (Cart c : cartItems) {
        ...
        // BUG: c.getVariant().getOfferPrice() can be null, causing NPE
        Long totalItemPrice = c.getQuantity() * c.getVariant().getOfferPrice();
        orderItem.setTotalPrice(totalItemPrice);
        orderItem.setPrice(c.getVariant().getOfferPrice());
        
        grandTotalPrice += totalItemPrice;
        
        // BUG: ignores quantity and null-safety
        baseSubTotal += c.getVariant().getPrice();
    }
    
    // BUG: Double Discounting (discountAmount is subtracted again!)
    Long grandTotal = subTotal + shippingCharge + taxAmount - discountAmount;
    
    // BUG: discountAmount set to the entire total
    order.setDiscountAmount(grandTotalPrice);
}
```

#### *After (Fully Corrected & Safe):*
```java
@org.springframework.transaction.annotation.Transactional
public void createOrderFromCart(Long userId, String id) {
    List<Cart> allItems = cartRepository.findByUserId(userId);
    Long subTotal = 0L;
    Long baseSubTotal = 0L;

    // 1. Filter out only cart items
    List<Cart> cartItems = new ArrayList<>();
    for (Cart c : allItems) {
        if (CartStatus.CART.equals(c.getStatus())) {
            cartItems.add(c);
        }
    }

    // 2. Empty cart guard
    if (cartItems.isEmpty()) {
        return; 
    }

    Optional<User> user = userRepository.findById(userId);

    // 3. Save the Parent Order first so we have an active ID
    Order order = new Order();
    order.setUserId(userId);
    order.setTransactionId(id);
    order.setOrderStatus("PLACED");
    order.setPaymentMethod("CARD");
    order.setPaymentStatus("PAID");

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

    // 4. Create and save order items with null-safety
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

        // Fall back to original price if there's no active discount offer
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
        // Multiply by quantity to get correct total base price
        baseSubTotal += c.getQuantity() * originalPrice;

        orderItemRepository.save(orderItem);
    }

    // 5. Calculate correct billing figures (Actual Subtotal + Extra Charges)
    Long discountAmount = baseSubTotal - subTotal;
    Long shippingCharge = (long) (subTotal * 0.01);
    Long taxAmount = (long) (subTotal * 0.03);
    Long grandTotal = subTotal + shippingCharge + taxAmount; // No double discounting!

    order.setTotalAmount(grandTotal);
    order.setDiscountAmount(discountAmount);
    order.setShippingCharge(shippingCharge);
    order.setTaxAmount(taxAmount);
    orderRepository.save(order);

    cartRepository.deleteAll(cartItems);
}
```

---

### **How to debug/find checkout pricing issues in the future:**

1. **Draw out the Math on Paper:** Always write down a mock scenario before coding. If you have an item priced at $100 on sale for $80:
   - What should the subtotal be? ($80)
   - What is the discount? ($20)
   - If you subtract the discount again at checkout, does it become $60? Yes -> **Double discounting detected.**
2. **Use Defensive Logging:** Always print the values of all your intermediate values:
   ```java
   log.info("Subtotal={}, Discount={}, Tax={}, Shipping={}, FinalTotal={}", 
            subTotal, discountAmount, taxAmount, shippingCharge, grandTotal);
   ```
   If you look at the console logs during testing and see weird numbers or zeroes, the logs will instantly tell you which variable had the wrong formula.
3. **Guard against Null DB Columns:** Database fields representing prices or discounts are often optional. Always assume `null` is a possibility and use fallbacks:
   ```java
   Long unitPrice = c.getVariant().getOfferPrice();
   if (unitPrice == null || unitPrice == 0) { ... }
   ```
4. **Assert Cart Items exist:** Never allow database write operations to proceed without checking if the input list contains items.

---

## 15. `Product.seller` Not Set During Product Creation (M15)

> [!IMPORTANT]  
> **Data Integrity Constraint Violation:** The `Product` entity strictly requires a `seller` mapping (`nullable = false`). Attempting to save a product without setting the seller will cause the database to reject the insert and throw a `DataIntegrityViolationException`.

**Error:**
When adding a new product via `ProductController.addProduct`, the product was being created and saved without linking it to the authenticated seller who created it. This resulted in an immediate database crash upon saving.

**Cause:**
The `Product` entity has a `@ManyToOne` relationship to `User` (the seller) which is mapped as `@JoinColumn(name = "seller_id", nullable = false)`. In `ProductService.addProduct()`, the `product.setSeller(...)` method was never called before calling `productRepository.save(product)`.

**Solution:**
Retrieve the authenticated seller's `userId` from the JWT token via the `@AuthenticationPrincipal` in the controller, pass it down to the service layer, fetch the corresponding `User` entity from the database, and assign it to the `Product` before saving.

*Controller Change (Inject Principal & Pass ID):*
```java
@PreAuthorize("hasRole('SELLER')")
@PostMapping(value = "/newProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ProductResponse> addProduct(
        @AuthenticationPrincipal CustomUserPrincipal principal, // Inject principal
        ...) {
    ...
    // Pass principal.getUserId() to service
    ProductResponse productResponse = productService.addProduct(principal.getUserId(), request, jsonNode, images);
    return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
}
```

*Service Change (Fetch User & Assign):*
```java
public ProductResponse addProduct(Long sellerId, ProductRequest productRequest, JsonNode jsonAttributes, MultipartFile[] images) {
    ...
    // Fetch Seller
    User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

    Product product = new Product();
    ...
    product.setSeller(seller); // Link the seller to satisfy nullable=false
    
    Product productSaved = productRepository.save(product);
    ...
}
```

### **How to Find This in the Future:**
1. **Always Check `@JoinColumn(nullable = false)` Requirements:** Whenever you create a new entity instance in your service layer, quickly check its Entity class (e.g., `Product.java`) for any fields marked with `nullable = false`. You must ensure these fields are populated before calling `.save()`.
2. **Watch for `DataIntegrityViolationException`:** If a save operation fails with this exception, the first thing to check is if you forgot to set a required foreign key or non-null column.
3. **Trace the User Context:** If an entity "belongs" to a user (like an order to a buyer, or a product to a seller), the service method creating it should almost always take the `userId` as a parameter to establish that relationship.

---

## 16. Pagination Missing on High-Volume Endpoints (M16)

> [!TIP]  
> **Memory & Performance Optimization:** Always use pagination (`Page<T>`) instead of lists (`List<T>`) for database queries that can return an unbounded or very large number of results (e.g., getting all products in a subcategory, or all items in a cart).

**Problem:**
Endpoints like `getProductBySubCategory()` and `getCartDetails()` were returning `List<T>`. If a subcategory had 50,000 products, the Spring Data repository would load all 50,000 entities into Java memory at once, causing terrible performance and potentially triggering an `OutOfMemoryError` (OOM).

**Solution / Theory:**
To solve this, we implemented Spring Data JPA Pagination:
1. **Repository Layer:** We changed the repository methods to accept a `Pageable` object and return a `Page<Entity>` (e.g., `Page<Product> findBySubCategoryId(Long id, Pageable p)`). This instructs the database to use SQL `LIMIT` and `OFFSET` clauses, meaning it only fetches the exact rows needed for the current page!
2. **Service Layer:** We construct a `PageRequest.of(page, size)` and pass it to the repository. 
   - *Theory:* Spring's `Page<T>` interface provides a very handy `.map()` function. This acts exactly like Java Streams. It loops through the `Page<Entity>` and safely converts it into a `Page<DTO>` (e.g., `Page<ProductResponse>`), keeping all the underlying pagination metadata intact (like `totalPages`, `totalElements`, etc.).
3. **Controller Layer:** We added `@RequestParam(defaultValue = "0") int page` and `@RequestParam(defaultValue = "10") int size` to the REST endpoints so the frontend can control which page they are viewing.

**Important Catch (The `OrderService` Bug):**
When we changed `CartRepository.findByUserId` to require pagination, it broke `OrderService.createOrderFromCart`.
*Why?* Because `OrderService` is calculating the final checkout price. It *needs* every single item in the cart at once; it cannot calculate a grand total on just "page 1" of the cart!
*Fix:* We created two separate repository methods:
- `Page<Cart> findByUserId(Long userId, Pageable pageable);` -> Used by `CartController` for UI display.
- `List<Cart> findAllByUserId(Long userId);` -> Used by `OrderService` for backend math.

---

## 17. Security Risk: Hardcoded Credentials & SSH Keys (M19)

> [!CAUTION]  
> **Security Vulnerability:** Never commit passwords, API keys, database URLs, or SSH `.pem` files directly into your codebase (like a `Jenkinsfile` or `application.properties`). If your GitHub repository is ever made public or compromised, your entire infrastructure is at risk.

**Solution:**
We removed the hardcoded IP addresses and local file paths (e.g., `C:/Users/.../Backend-Pair.pem`) from the CI/CD pipeline. 
Instead, we rely on **Environment Secrets** (like Jenkins Credentials). 
In Jenkins, you upload the `.pem` file as an "SSH Username with private key" credential, and use the `withCredentials` block to dynamically inject it into the pipeline at runtime. Jenkins securely creates a temporary file for the SSH command and destroys it immediately after.

---

## 18. Performance Issue: String Concatenation in Logs (M20)

> [!TIP]  
> **Memory Optimization:** Never use the `+` operator to concatenate strings inside SLF4J log statements. Always use parameterized logging with `{}` placeholders.

**Error Example:**
```java
log.info("Dashboard for seller " + sellerId); // BAD
```
If logging is turned off (e.g., the level is set to `ERROR`), Java still wastes CPU and memory constructing that string in the background before SLF4J ignores it.

**Solution Example:**
```java
log.info("Dashboard for seller {}", sellerId); // GOOD
```
SLF4J will only construct the final string if it actually decides to print the log! This saves massive amounts of memory across millions of requests.

---

## 19. Data Privacy: Logging Sensitive Data (M25)

> [!WARNING]  
> **Data Privacy Violation:** Never log sensitive user information (like raw JSON bodies containing passwords, full JWT tokens, or credit card info) at the `INFO` level.

**Problem:**
We were logging raw JSON requests during product creation and user emails during login using `log.info(...)`. In a production environment, `INFO` logs are stored in databases like Elasticsearch or AWS CloudWatch. If a developer or hacker gets access to those logs, they can see user passwords or exploit raw JWT tokens.

**Solution / Theory (Log Levels):**
We changed these sensitive logs to `log.debug(...)` or removed them entirely. 
- `INFO`: General application flow (e.g., "Server started", "Order placed successfully"). These are always printed in production.
- `DEBUG`: Deep technical details needed only for troubleshooting (e.g., "Raw JSON payload: {...}"). 

By default, Spring Boot does not print `DEBUG` logs in production. If a bug occurs, you temporarily turn on `DEBUG` logging, find the issue, and turn it off again, ensuring sensitive data doesn't permanently live in your log files!

---

## 20. Performance: Missing `@Transactional(readOnly = true)` (M26)

> [!TIP]  
> **Database Optimization:** Always add `@Transactional(readOnly = true)` to service methods that only fetch data (e.g., `getCartDetails`, `getAllProducts`).

**Theory:**
When Hibernate loads an entity from the database during a normal transaction, it takes a "snapshot" of that entity and keeps it in memory. At the end of the transaction, it compares the current state to the snapshot to see if you changed anything (this is called "dirty checking"). If you did, it automatically fires an `UPDATE` statement.
Dirty checking is expensive! By declaring a transaction as `readOnly = true`, you tell Hibernate, "I promise I am not going to modify this data," so Hibernate completely skips creating the snapshot and doing the dirty checking. This makes read operations significantly faster and uses less memory.

---

## 21. Architecture: Primitive vs. Boxed Data Types for Entity IDs (M27)

> [!IMPORTANT]  
> **JPA Best Practice:** Never use primitive data types (like `long` or `int`) for primary keys (`@Id`) in JPA Entities. Always use their Object wrappers (`Long` or `Integer`).

**Problem:**
In `Address.java`, the primary key was defined as `private long id;`. 

**Theory:**
A primitive `long` can never be `null`; its default value is `0`. 
When you call `repository.save(entity)`, Hibernate checks the ID to figure out if it should generate an `INSERT` (new record) or an `UPDATE` (existing record). 
- If the ID is `null`, Hibernate knows it's a brand new entity and `INSERT`s it.
- If the ID is `0` (because of a primitive `long`), Hibernate thinks "Oh, this is an existing entity with an ID of 0," and will try to fire an `UPDATE` statement, which will fail or corrupt your database if ID 0 doesn't exist!

**Solution:**
We changed `private long id;` to `private Long id;` (and updated the getter/setter to match). A `Long` object defaults to `null`, allowing Hibernate's `save()` logic to work perfectly.
