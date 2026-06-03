# E-Commerce Backend & Database Knowledge Base: Errors, Mistakes, and Theory

This document is a comprehensive compilation of all technical mistakes, runtime errors, and key backend/database theories encountered during the development of this E-Commerce platform. It serves as a checklist and knowledge base to prevent regressions and guide future system extensions.

---

## ☕ Part 1: Java (Core Concepts, Coding Mistakes & Serialization)

### 1. Jackson Serialization Mismatch with `JsonNode`
* **The Mistake / Error**: When retrieving products and variant attributes, returning a `JsonNode` (like `variant_attributes` column mapped using `@JdbcTypeCode(SqlTypes.JSON)`) directly in a REST controller DTO caused Jackson to serialize the internal class metadata of `JsonNode` (such as `nodeType`, `array`, `object`, etc.) rather than the actual flat JSON key-value pairs.
* **The Solution**: 
  Convert the `JsonNode` into a standard Java collection (like a `Map` or `List`) through string deserialization before returning it in the DTO:
  ```java
  Object plainAttributes = null;
  try {
      if (productVariants.getVariantAttributes() != null) {
          // Force plain Java collections (Map/List) instead of JsonNode metadata
          plainAttributes = new ObjectMapper().readValue(
              productVariants.getVariantAttributes().toString(),
              Object.class
          );
      }
  } catch (Exception ex) {
      log.error("Could not convert variantAttributes JsonNode: " + ex.getMessage());
  }
  productResponse.setVariantAttributes(plainAttributes);
  ```
* **The Theory to Remember**:
  Jackson's `JsonNode` is a container object. Direct serialization of `JsonNode` without conversion can expose Jackson's class structure structure depending on the configuration. Deserializing the node's string value into `Object.class` forces Jackson to resolve it dynamically as native Java types (`Map<String, Object>` or `List<Object>`), which serialize cleanly into standard JSON format.

### 2. Hibernate `LazyInitializationException` on Lazily Loaded Collections
* **The Mistake / Error**: Accessing nested collections like `user.getRoles()` outside the transaction scope (e.g., inside the Controller or in `UserDetailsService` after the transaction closed) threw a `LazyInitializationException` because the Hibernate session was already closed.
* **The Solution**: 
  Ensure the collection is explicitly initialized inside a `@Transactional` service method using `org.hibernate.Hibernate.initialize()`:
  ```java
  @Transactional
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
      User user = userRepository.findByEmail(email);
      if (user == null) {
          throw new UsernameNotFoundException("User not found");
      }
      
      // Explicitly initialize the lazy collection within the active session
      org.hibernate.Hibernate.initialize(user.getRoles());

      String[] rolesArray = user.getRoles().stream()
              .map(Role::getRoleName)
              .toArray(String[]::new);

      return org.springframework.security.core.userdetails.User.builder()
              .username(user.getEmail())
              .password(user.getPassword())
              .disabled(!user.isEnabled())
              .roles(rolesArray)
              .build();
  }
  ```
* **The Theory to Remember**:
  By default, `@OneToMany` and `@ManyToMany` relations are fetched lazily (`FetchType.LAZY`) to prevent pulling the entire database on every query. If you access a lazy relation after the session is closed, Hibernate cannot execute the SQL to fetch it. Under `@Transactional`, the session stays open, and calling `Hibernate.initialize()` forces the proxy collection to load. An alternative solution is using `JOIN FETCH` inside custom JPQL queries.

### 3. Lombok and MapStruct Processor Conflicts in Maven
* **The Mistake / Error**: Compilation failures or empty mapped fields occurred because Lombok getters/setters were not generated before MapStruct attempted to build the mapping implementations.
* **The Solution**: 
  Explicitly configure the compilation execution order in `pom.xml` under the `maven-compiler-plugin` annotation processor paths, putting Lombok first:
  ```xml
  <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.10.1</version>
      <configuration>
          <source>17</source>
          <target>17</target>
          <annotationProcessorPaths>
              <path>
                  <groupId>org.projectlombok</groupId>
                  <artifactId>lombok</artifactId>
                  <version>1.18.28</version>
              </path>
              <path>
                  <groupId>org.mapstruct</groupId>
                  <artifactId>mapstruct-processor</artifactId>
                  <version>1.5.5.Final</version>
              </path>
          </annotationProcessorPaths>
      </configuration>
  </plugin>
  ```
* **The Theory to Remember**:
  Both MapStruct and Lombok operate during the compilation phase using Annotation Processors. Lombok modifies the AST to generate getters, setters, and constructors. MapStruct reads those getter/setter methods to generate mapper implementations. If MapStruct runs before Lombok, it finds no getters/setters and fails or generates empty mapper bodies. Lombok *must* run first.

### 4. Defensive Exception Handling and Wrapping for External SDKs
* **The Mistake / Error**: Direct leakage of raw Stripe/AI library exceptions caused raw stack traces to be returned to the client, leading to security issues and confusing client-side errors (e.g. 500 status codes instead of a service unavailability status).
* **The Solution**: 
  Catch library-specific checked exceptions (e.g., `StripeException`) and map them to HTTP `502 Bad Gateway` or `503 Service Unavailable` with clean, localized error responses:
  ```java
  try {
      PaymentIntent intent = PaymentIntent.create(params);
      // Logic ...
  } catch (StripeException e) {
      log.error("StripeException occurred during payment creation: {}", e.getMessage());
      return ResponseEntity.status(502).body(Map.of("error", "Payment service temporarily unavailable."));
  }
  ```
* **The Theory to Remember**:
  External dependency failures should never lead to generic Internal Server Errors (500). System owners treat third-party networks as unreliable dependencies. Proper exception mapping (e.g. `502 Bad Gateway` for Stripe down, `503 Service Unavailable` for connection errors, `504 Gateway Timeout` for slow LLM calls) informs clients accurately and isolates failures.

---

## 🍃 Part 2: Spring Boot (Framework Architecture & Configuration)

### 1. The Threading Classpath Mismatch: `ClassNotFoundException: org.springframework.boot.thread.Threading`
* **The Mistake / Error**: The application failed to boot with the error:
  `BeanDefinitionStoreException: Failed to process import candidates... Could not find class [org.springframework.boot.thread.Threading]`
  This occurs when attempting to configure virtual threads or asynchronous execution using configuration classes or annotations that don't match the version classpath of the parent Spring Boot starter.
* **The Solution**: 
  Instead of importing internal, version-specific thread configuration classes manually, configure virtual threads declaratively in `application.properties` (supported natively in Spring Boot 3.2+ and Java 21+):
  ```properties
  spring.threads.virtual.enabled=true
  ```
  And ensure the `@EnableAsync` annotation is added directly to a `@Configuration` class or the main application class.
* **The Theory to Remember**:
  Virtual Threads (Project Loom) are natively supported starting from Spring Boot 3.2. Reference to internal boot threading wrappers (`org.springframework.boot.thread.Threading`) are highly fragile and break between minor updates. Declarative activation via properties lets the framework auto-configure the task executor safely.

### 2. Secondary DataSource Conflict with Spring AI Auto-Configuration
* **The Mistake / Error**: When introducing a secondary MariaDB database for the AI Vector Store while keeping PostgreSQL as the primary transactional database, the application failed to start because Spring AI automatically tried to build vector store schemas on the PostgreSQL `@Primary` datasource (which failed because PostgreSQL does not support MariaDB syntax).
* **The Solution**: 
  1. Exclude the automatic vector store configuration in the main application class:
     ```java
     @SpringBootApplication(exclude = { MariaDbStoreAutoConfiguration.class })
     ```
  2. Define and qualify separate DataSources in a configuration class (`DataSourceConfig.java`):
     ```java
     @Bean
     @Primary
     @ConfigurationProperties("spring.datasource.postgres")
     public DataSourceProperties postgresDataSourceProperties() { return new DataSourceProperties(); }

     @Bean(name = "postgresDataSource")
     @Primary
     public DataSource postgresDataSource(@Qualifier("postgresDataSourceProperties") DataSourceProperties props) {
         return props.initializeDataSourceBuilder().build();
     }

     @Bean
     @ConfigurationProperties("spring.datasource.mariadb")
     public DataSourceProperties mariadbDataSourceProperties() { return new DataSourceProperties(); }

     @Bean(name = "mariadbDataSource")
     public DataSource mariadbDataSource(@Qualifier("mariadbDataSourceProperties") DataSourceProperties props) {
         return props.initializeDataSourceBuilder().build();
     }
     ```
  3. Wire the MariaDB vector store bean manually to use the secondary database (`aiConfig.java`):
     ```java
     @Bean
     public VectorStore vectorStore(
             @Qualifier("mariadbJdbcTemplate") JdbcTemplate mariadbJdbcTemplate,
             EmbeddingModel embeddingModel) {
         return MariaDBVectorStore.builder(mariadbJdbcTemplate, embeddingModel)
                 .initializeSchema(true)
                 .distanceType(MariaDBVectorStore.MariaDBDistanceType.COSINE)
                 .dimensions(1024)
                 .build();
     }
     ```
* **The Theory to Remember**:
  Spring Boot auto-configuration aggressively scans the classpath. If it finds the MariaDB vector store starter, it configures a `VectorStore` using the default (`@Primary`) `DataSource`. By excluding `MariaDbStoreAutoConfiguration` and explicitly injecting a specialized `JdbcTemplate` bound to MariaDB, we keep the AI data store fully isolated from our PostgreSQL transactional database.

### 3. JWT Stateless Auth vs. OAuth2 Default Session Creation
* **The Mistake / Error**: Under default OAuth2 configurations, Spring Security keeps an active HTTP session alive on the server after authentication. This violates the stateless JWT architecture, risking session leaks and synchronization errors.
* **The Solution**: 
  Invalidate the HTTP session and clear the Spring Security context immediately upon generating and redirecting with the JWT tokens inside the `OAuthSuccessHandler`:
  ```java
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
      UserDetails userDetails = userService.processOAuthPostLogin(email);
      String accessToken = jwtUtil.generateToken(userDetails);
      String refreshToken = jwtUtil.generateRefreshToken(userDetails);

      // Enforce stateless behavior by clearing the session created by OAuth
      SecurityContextHolder.clearContext();
      HttpSession session = request.getSession(false);
      if (session != null) {
          session.invalidate();
      }

      response.sendRedirect(redirectUrl + accessToken + "&refreshToken=" + refreshToken);
  }
  ```
* **The Theory to Remember**:
  OAuth2 login flows require HTTP sessions (e.g., to store authorization state between redirects). However, once the authentication is complete and the server issues a JWT token to the client, the session is obsolete. Invalidation ensures that subsequent client requests are validated statelessly via the `Authorization: Bearer <token>` header, reducing server memory footprint and session hijacking threats.

### 4. Cache Management Pitfalls: Stale Data, Evictions, and Omissions
* **The Mistake / Error**: 
  1. Adding new products successfully in the DB, but queries returning product lists still showed stale data. This occurred due to missing Cache Eviction on creation.
  2. **Hidden Gotcha**: Deleting a product (e.g., using `removeProducts` in `ProductService`) did not trigger eviction, meaning the deleted product remained in the cache until a new product addition occurred.
* **The Solution**: 
  Apply `@CacheEvict(value = "products", allEntries = true)` on both creation, modification, and deletion methods:
  ```java
  @Transactional
  @CacheEvict(value = "products", allEntries = true)
  public ProductResponse addProduct(ProductRequest productRequest, JsonNode jsonAttributes, MultipartFile[] images) {
      // Save product...
  }

  @Transactional
  @CacheEvict(value = "products", allEntries = true)
  public String removeProducts(Long productId) {
      productRepository.deleteById(productId);
      return "Product is removed";
  }

  @Cacheable("products")
  public List<Product> getAllProducts() {
      return productRepository.findAll();
  }
  ```
* **The Theory to Remember**:
  Caching improves read scalability but introduces synchronization challenges. Modifying or deleting data without evicting the cache leads to state inconsistency. `@CacheEvict(allEntries = true)` removes all cached elements under a namespace when a write/delete occurs, guaranteeing that subsequent reads fetch fresh data from the database.

### 5. Spring AI Tool Parameter Binding: Primitive vs. Object parameters
* **The Mistake / Error**: Binding a complex entity class (like `Ticket`) directly as a `@ToolParam` inside a `@Tool` method caused binding errors. LLMs generate JSON structures to represent method arguments; if the target object contains nested attributes or non-standard types, the LLM cannot consistently serialize the correct JSON format required by Jackson to construct the Java object, leading to deserialization failures.
* **The Solution**:
  Flatten the tool parameter signature into simple, primitive data types (`String`, `Integer`, etc.), and programmatically construct the complex domain object inside the Java method:
  ```java
  @Tool(description = "Create a new support ticket in the database")
  public Ticket createTicketTool(
          @ToolParam(description = "Short summary of the issue") String summary,
          @ToolParam(description = "Detailed description") String description,
          @ToolParam(description = "Issue category") String category,
          @ToolParam(description = "Priority: P1, P2, or P3") String priority,
          @ToolParam(description = "User email address") String email) {

      Ticket ticket = new Ticket();
      ticket.setSummary(summary);
      ticket.setDescription(description);
      ticket.setCategory(category);
      ticket.setPriority(parsePriority(priority));
      ticket.setEmail(email);
      ticket.setStatus(Status.OPEN);
      return ticketService.createTicket(ticket);
  }
  ```
  *(Note: Methods like `updateTicket(Ticket ticket)` that still accept a raw `Ticket` parameter are prone to these binding errors and should be refactored to use flat parameters).*
* **The Theory to Remember**:
  Large Language Models excel at generating simple key-value structures. Complex nested objects increase the prompt's cognitive load and lead to formatting errors. By using flat, primitive method signatures for tool calling, we create a robust, error-tolerant API contract for the LLM.

### 6. Resilience4j Fallback Signature Matching
* **The Mistake / Error**: The application failed to startup or threw runtime exceptions because Resilience4j could not register or find the fallback method for `@CircuitBreaker(name = "...", fallbackMethod = "...")`.
* **The Solution**:
  The fallback method's parameter list must match the target method's parameter list **exactly**, plus one additional parameter at the very end of type `Throwable` (or a subclass of it). The return types must also match:
  ```java
  // Target Method
  @CircuitBreaker(name = "ollamaService", fallbackMethod = "fallbackResponse")
  public String getResponseFromAssistsnt(String query, String conversationId) {
      // ChatClient LLM logic...
  }

  // Fallback Method (Matches signature + Throwable)
  private String fallbackResponse(String query, String conversationId, Throwable t) {
      log.info("Ollama call failed: {}", t.getMessage());
      return "Ollama response failed (Service Down)";
  }
  ```
* **The Theory to Remember**:
  Resilience4j uses reflection at startup to build proxy intercepts. It searches for a method with the name specified in the `fallbackMethod` attribute that accepts the target method's arguments followed by the throwing exception. If the signature doesn't match, startup fails with a configuration error.

### 7. Spring AI Advanced RAG & Chat Client Configuration
* **System Design & Implementation**:
  1. **Document ETL Pipeline (`DataLoaderImpl.java`)**: 
     Loads documents from JSON (`JsonReader`) and PDF (`PagePdfDocumentReader`). The documents are split into chunks using a `TokenTextSplitter` configured with specific chunk boundaries:
     ```java
     var splitter = new TokenTextSplitter(30, 40, 10, 5000, true);
     ```
     These chunks are then transformed and saved into the vector store (`vectorStore.add(transformedDocument)`).
  2. **Retrieval Augmentation Advisor (RAG)**:
     Configured dynamically in `ChatService.getResponse()` using `RetrievalAugmentationAdvisor`:
     - **Pre-Retrieval**: `RewriteQueryTransformer` rewrites the user query to optimize vector matching, and `MultiQueryExpander` expands it into multiple queries.
     - **Retrieval**: `VectorStoreDocumentRetriever` performs a similarity search on MariaDB VectorStore (topK=3, threshold=0.3).
     - **Post-Retrieval**: `ConcatenationDocumentJoiner` merges chunks, and `ContextualQueryAugmenter` adds the context to the LLM system prompt.
  3. **Chat Client Advisors**:
     Configured in `aiConfig.java`:
     - `MessageChatMemoryAdvisor`: Associates an `InMemoryChatMemoryRepository` (with a sliding window of 10 messages) using `ChatMemory.CONVERSATION_ID` to make the stateless LLM stateful.
     - `SimpleLoggerAdvisor`: Logs input prompts and output answers.
     - `SafeGuardAdvisor`: Configured to reject or filter prompts containing blocked keywords (e.g., `List.of("games")`).

---

## 🗄️ Part 3: Other Info (Databases, Flyway, CORS, & Docker)

### 1. Flyway Migration vs. Hibernate DDL Auto Mismatch
* **The Mistake / Error**: Table initialization failed or schemas drifted when using Flyway because Hibernate was configured with `spring.jpa.hibernate.ddl-auto=update`. Hibernate attempted to auto-modify columns before Flyway recorded the changes, creating synchronization deadlocks.
* **The Solution**: 
  Change the configuration to `validate` in local development and production. Let Flyway handle all modifications:
  ```properties
  # In application-local.properties & application-prod.properties
  spring.jpa.hibernate.ddl-auto=validate
  spring.flyway.enabled=true
  spring.flyway.baseline-on-migrate=true
  ```
* **The Theory to Remember**:
  In professional database lifecycle management, a schema migration tool (Flyway/Liquibase) owns the database schema. `ddl-auto=update` is unsafe because it generates database schemas implicitly based on entity definitions without leaving a versioned history. Setting `ddl-auto=validate` forces Hibernate to only inspect and verify that entities match the table structures generated by Flyway SQL migrations.

### 2. Concurrency Race Conditions & Atomic Database Updates
* **The Mistake / Error**: In high-concurrency order placements, stock overselling occurred because threads read the stock size in Java, validated it, and wrote the decremented stock back. If two requests read a stock of 1 simultaneously, both validated it as sufficient and decreased it, leading to a final stock of -1.
* **The Solution**: 
  Move the stock verification and decrement operation into an atomic database statement inside `ProductVariantsRepository.java`:
  ```java
  @Modifying
  @Transactional
  @Query("UPDATE ProductVariants v SET v.quantity = v.quantity - :qty WHERE v.id = :variantId AND v.quantity >= :qty")
  int decreaseStock(@Param("variantId") Long variantId, @Param("qty") Long qty);
  ```
  Then verify in the service layer if the affected row count is greater than zero:
  ```java
  int updatedRows = productVariantsRepository.decreaseStock(variantId, quantity);
  if (updatedRows == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock or variant not found");
  }
  ```
* **The Theory to Remember**:
  Using application-level check-then-act logic is highly vulnerable to race conditions under load. By moving the check (`WHERE quantity >= :qty`) and modification into a single database update statement, the database engine enforces transactional isolation. Since database write locks are atomic per row, it guarantees that no two transactions can overcommit stock.

### 3. Business Logic Leak: Cart vs. Wishlist Segregation
* **The Mistake / Error**: Wishlist items were accidentally converted into orders during the checkout process. This occurred because both cart items (to buy now) and wishlist items (saved for later) are stored in the same database table (`cart`), segregated only by a `status` column (`CartStatus.CART` vs. `CartStatus.WISHLIST`). The `createOrderFromCart` method originally loaded all items using `cartRepository.findByUserId(userId)` without filtering by status.
* **The Solution**:
  Explicitly filter the fetched items by the `CartStatus.CART` status before processing the order, or create a query method that filters by status at the repository layer:
  ```java
  // OrderService.java
  List<Cart> allItems = cartRepository.findByUserId(userId);
  List<Cart> cartItems = new ArrayList<>();
  for (Cart c : allItems) {
      if (CartStatus.CART.equals(c.getStatus())) {
          cartItems.add(c);
      }
  }
  if (cartItems.isEmpty()) return;
  
  // Create orders using cartItems...
  cartRepository.deleteAll(cartItems); // Only deletes cart items, leaves wishlist intact
  ```
* **The Theory to Remember**:
  Shared-table entity design (e.g. placing Cart and Wishlist items in the same table) is a common database consolidation pattern. However, it shifts the responsibility of state isolation to the application layer. Any database read must explicitly assert the status filter. Leaking states between these domains leads to serious business failures (like users buying their wishlist items).

### 4. Advanced PostgreSQL Indexing for JSONB & Partial Conditions
* **The Mistake / Error**: Slow filtering queries over dynamic variant attributes stored as JSON columns and slow stock checks as the product tables grew.
* **The Solution**: 
  Configure a partial GIN index and filtered indexes in the schema migrations (`V0__allschema.sql`):
  ```sql
  -- Fast JSONB attribute lookups
  CREATE INDEX idx_variant_json ON public.product_variant USING gin (variant_attributes jsonb_path_ops);

  -- Partial Index: Indexes only variants that are in stock
  CREATE INDEX idx_in_stock ON public.product_variant USING btree (variant_id) WHERE (quantity > 0);
  ```
* **The Theory to Remember**:
  * **GIN (Generalized Inverted Index)**: Standard B-Tree indexes cannot index JSON columns. GIN indexes with `jsonb_path_ops` index the keys and values nested within JSON structures, transforming O(N) full-table scans into O(log N) lookup queries.
  * **Partial Index**: Only indexes rows matching a `WHERE` condition. Since an e-commerce catalog only queries available products for users, indexing only rows with `quantity > 0` significantly reduces the index size and speeds up scans.

### 5. PostgreSQL Stored Procedures & Functions for JSON Unpacking
* **Implementation Details**:
  To automate variant generation from unstructured JSON configurations, the system uses a stored procedure and function:
  ```sql
  CREATE PROCEDURE public.generate_variants(IN pid bigint)
      LANGUAGE plpgsql
      AS $$
  BEGIN
      INSERT INTO public.product_variant(product_id, variant_attributes, quantity)
      SELECT pid, v, 0
      FROM jsonb_array_elements(
          (SELECT json_attributes FROM product WHERE id = pid)
      ) AS v;
  END;
  $$;
  ```
* **The Theory to Remember**:
  Processing collection expansions (e.g. generating variants based on product attributes) on the database side utilizing PL/pgSQL procedures is highly efficient. `jsonb_array_elements()` unpacks a JSONB array into a set of rows. This minimizes Java-database roundtrips, reducing CPU cycles and serialization overhead on the application server.

### 6. CORS Wildcards (`*`) with Credential Sharing
* **The Mistake / Error**: Configuring CORS using a wildcard `*` while allowing credentials:
  ```java
  // INCORRECT
  configuration.setAllowedOrigins(List.of("*"));
  configuration.setAllowCredentials(true);
  ```
  Browsers will reject requests with this combination because allowing wildcards with credentials enables cross-origin credential stealing.
* **The Solution**: 
  Instead of wildcards, use `allowedOriginPatterns` to explicitly validate incoming domains:
  ```java
  configuration.setAllowedOriginPatterns(java.util.List.of("http://localhost:[*]", "https://*.yourdomain.com"));
  configuration.setAllowCredentials(true);
  ```
* **The Theory to Remember**:
  Under the CORS specification, browsers block wildcard origins if `Access-Control-Allow-Credentials` is set to `true`. This prevents third-party malicious sites from fetching cookie-authenticated data from your API. Origin patterns resolve this restriction securely by matching valid environments while keeping auth headers allowed.

---

## 🚀 Key Architectural Checklist for Future Extensions

1. **Transactional Boundaries**: Always mark services modifying multiple repositories with `@Transactional` to avoid partial data commits.
2. **Flyway Migrations**: Never modify tables manually in development databases. Always create a new versioned migration script (`V1__description.sql`) under `src/main/resources/db/migration`.
3. **Resilience & Timeouts**: Configure circuit breakers (`resilience4j.circuitbreaker`) and retries on any service calling external entities (like Stripe or LLM APIs) to prevent slow calls from consuming Tomcat request threads.
4. **Traceability (Actuator & MDC)**: Integrate Spring Boot Actuator and Prometheus for metrics. Utilize MDC logging patterns (`%X{traceId}`) to map trace IDs to log statements, allowing you to trace client requests from gateway entries down to database operations.
5. **Flat AI Tool Parameters**: When creating custom Spring AI `@Tool` components, always use flat, primitive parameters. Avoid passing raw entity models to tools to prevent LLM serialization issues.
6. **Cache Eviction Checklist**: Ensure that every state-modifying database action (insert, update, and delete) has a matching `@CacheEvict(allEntries = true)` on the respective cache names.
7. **Logical Table Partitioning**: If using a single table for multiple entity statuses (e.g. cart vs wishlist), document the status values and enforce status-based filtering on all read/update transactions.
