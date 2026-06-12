# ShopSphere - E-Commerce Microservices Architecture
## Comprehensive Project Documentation

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Microservices Description](#microservices-description)
4. [Design Patterns](#design-patterns)
5. [Advanced Concepts & Technologies](#advanced-concepts--technologies)
6. [Kafka Events](#kafka-events)
7. [API Documentation](#api-documentation)
8. [Running the Project](#running-the-project)
9. [Monitoring & Logging](#monitoring--logging)
10. [Best Practices](#best-practices)

---

## Project Overview

**ShopSphere** is a production-ready e-commerce microservices platform built with Spring Boot and Spring Cloud. It demonstrates enterprise-grade distributed system architecture with service discovery, event-driven communication, circuit breakers, distributed tracing, and comprehensive monitoring.

### Tech Stack
- **Backend**: Spring Boot 3.1.5, Spring Cloud 2022.0.4, Java 17
- **Service Discovery**: Eureka
- **API Gateway**: Spring Cloud Gateway
- **Configuration**: Spring Cloud Config Server
- **Messaging**: Apache Kafka
- **Cache**: Redis
- **Database**: MySQL (Database per Service pattern)
- **Monitoring**: Prometheus, Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Distributed Tracing**: Zipkin
- **Resilience**: Resilience4j (Circuit Breaker, Retry)
- **Containerization**: Docker & Docker Compose

---

## Architecture Overview

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    API Gateway       │
                    │  (Port 8080)         │
                    │  - Rate Limiting     │
                    │  - JWT Auth          │
                    │  - Request Routing   │
                    └──────────┬───────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
   ┌─────────────┐        ┌─────────────┐      ┌─────────────┐
   │ Eureka      │        │ Config      │      │ Services    │
   │ Server      │        │ Server      │      │ (8001-8008) │
   │ (8761)      │        │ (8888)      │      │             │
   └─────────────┘        └─────────────┘      └─────────────┘
        │                                            │
        └────────────────┬──────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
    ┌─────────┐    ┌──────────┐    ┌────────┐
    │ Kafka   │    │ Redis    │    │ MySQL  │
    │Message  │    │ Cache    │    │Database│
    │ Broker  │    │          │    │        │
    └─────────┘    └──────────┘    └────────┘

    ┌─────────┐    ┌──────────┐    ┌────────┐
    │ Zipkin  │    │Prometheus│   │Grafana │
    │ Tracing │    │Metrics   │   │Dashboard
    └─────────┘    └──────────┘    └────────┘
```

### Core Architectural Principles

**1. Microservices Architecture**
- Each service has its own database (Database per Service Pattern)
- Services are independently deployable and scalable
- Loose coupling with high cohesion

**2. Service Communication**
- Synchronous: REST via Feign Client for inter-service calls
- Asynchronous: Kafka for event-driven communication
- Circuit Breaker pattern for resilience

**3. Data Consistency**
- Saga Pattern for distributed transactions
- Event sourcing for audit trails
- Eventual consistency model

---

## Microservices Description

### 1. **API Gateway** (Port: 8080)
**Purpose**: Single entry point for all client requests

**Key Features**:
- Request routing to appropriate microservices
- JWT token validation
- Rate limiting per user
- Request/Response logging
- API versioning support

**Technologies**:
- Spring Cloud Gateway
- Spring Security
- Redis for rate limiting

**Key Files**:
- `JwtAuthenticationFilter.java`: Custom JWT filter
- `application.yml`: Route configuration

---

### 2. **Eureka Server** (Port: 8761)
**Purpose**: Service registry and discovery

**Key Features**:
- Service registration
- Client-side service discovery
- Health checking
- Heartbeat mechanism

**Configuration**:
```yaml
eureka.instance.hostname=localhost
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

**Client Services** register automatically with Eureka via:
```properties
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

---

### 3. **Config Server** (Port: 8888)
**Purpose**: Centralized configuration management

**Key Features**:
- Externalized configuration
- Environment-specific properties
- Dynamic property refresh
- Git-backed configuration repository

**Usage**:
Services fetch configuration from Config Server on startup.

---

### 4. **User Service** (Port: 8001)
**Purpose**: User authentication and profile management

**Responsibilities**:
- User registration
- Authentication (JWT generation)
- Profile management
- User caching

**Database**: MySQL - `shopsphere_user_db`

**Design Patterns Used**:
1. **Builder Pattern**: `UserBuilder.java` - Flexible object construction
2. **Factory Pattern**: `UserDTOFactory.java` - DTO conversion
3. **Strategy Pattern**: `PasswordEncodingStrategy.java` - Pluggable password encoding

**Key Features**:
- Circuit Breaker on database calls
- Redis caching for user data
- JWT token generation
- Resilience4j retry mechanism

**Endpoints**:
```
POST   /users/register          - Register new user
POST   /users/login             - User login (returns JWT)
GET    /users/{userId}          - Get user details
PUT    /users/{userId}          - Update user profile
```

---

### 5. **Product Service** (Port: 8002)
**Purpose**: Product catalog management

**Responsibilities**:
- Product CRUD operations
- Inventory information
- Product search and filtering
- Category management

**Database**: MySQL - `shopsphere_product_db`

**Key Features**:
- Redis caching for products
- Circuit breaker for external calls
- Bulk product operations

---

### 6. **Cart Service** (Port: 8006)
**Purpose**: Shopping cart management

**Responsibilities**:
- Add/remove items from cart
- View cart contents
- Cart total calculation
- Temporary cart storage

**Storage**: Redis (session-based)

**Key Features**:
- No database persistence (cache-based)
- Session management
- Cart expiration

---

### 7. **Order Service** (Port: 8003)
**Purpose**: Order creation and management

**Responsibilities**:
- Order creation
- Order status tracking
- Order history
- Saga orchestration

**Database**: MySQL - `shopsphere_order_db`

**Saga Pattern Implementation**:
```
Order Creation → Inventory Reservation → Payment → Confirmation
                 (async via Kafka)
```

**Design Patterns**:
- **Saga Pattern**: Distributed transaction management
- **Observer Pattern**: Event listening for order events

**Key Features**:
- Idempotency key support
- CompletableFuture for async operations
- Feign Client calls to Payment Service
- Kafka event publishing

**Endpoints**:
```
POST   /orders                  - Create order
GET    /orders/{orderId}        - Get order details
GET    /orders?userId={userId}  - Get user's orders
PUT    /orders/{orderId}/cancel - Cancel order
```

---

### 8. **Payment Service** (Port: 8004)
**Purpose**: Payment processing

**Responsibilities**:
- Process payments
- Payment status tracking
- Payment method management
- Transaction logging

**Database**: MySQL - `shopsphere_payment_db`

**Key Features**:
- Idempotency support (prevent duplicate charges)
- Feign Client for order verification
- Kafka event publishing

---

### 9. **Inventory Service** (Port: 8005)
**Purpose**: Inventory management

**Responsibilities**:
- Stock tracking
- Inventory reservation
- Stock deduction on purchase
- Restock management

**Database**: MySQL - `shopsphere_inventory_db`

**Key Features**:
- Reserved inventory tracking
- Compensation logic for failed orders
- Kafka event consumption

---

### 10. **Notification Service** (Port: 8007)
**Purpose**: Event-driven notifications

**Responsibilities**:
- Order confirmation emails
- Payment notifications
- Inventory alerts
- Customer communications

**Key Features**:
- Kafka consumer for multiple events
- Email integration
- Template-based notifications
- No database persistence

---

### 11. **Review Service** (Port: 8008)
**Purpose**: Product reviews and ratings

**Responsibilities**:
- Review creation
- Rating management
- Review moderation
- Review retrieval

**Database**: MySQL - `shopsphere_review_db`

---

## Design Patterns

### 1. **Builder Pattern**
**Location**: `UserBuilder.java`

**Purpose**: Provide a flexible way to construct complex objects

**Implementation**:
```java
User user = new UserBuilder()
    .email("user@example.com")
    .username("username")
    .password("password")
    .firstname("John")
    .lastname("Doe")
    .build();
```

**Benefits**:
- Clean API
- Immutability
- Readability

---

### 2. **Factory Pattern**
**Location**: `UserDTOFactory.java`

**Purpose**: Create objects without specifying exact classes

**Implementation**:
```java
UserDTO dto = UserDTOFactory.toDTO(user);
```

**Benefits**:
- Decoupling creation logic
- Centralized conversion logic
- Easy to maintain

---

### 3. **Strategy Pattern**
**Location**: `PasswordEncodingStrategy.java`, `BCryptPasswordEncodingStrategy.java`

**Purpose**: Define a family of algorithms and make them interchangeable

**Implementation**:
```java
public interface PasswordEncodingStrategy {
    String encode(String password);
    boolean matches(String rawPassword, String encodedPassword);
}
```

**Implementations**:
- BCrypt (current)
- Extensible for other algorithms (Argon2, PBKDF2)

---

### 4. **Observer Pattern**
**Location**: `OrderEventConsumer.java`

**Purpose**: Define one-to-many dependency for event notifications

**Implementation**:
- Kafka topics: `order-created-topic`, `payment-completed-topic`
- Notification Service subscribes to order events

---

### 5. **Saga Pattern**
**Location**: `OrderSaga.java`

**Purpose**: Coordinate distributed transactions across services

**Flow**:
```
1. Order Service creates order → publishes OrderCreatedEvent
2. Inventory Service reserves stock → publishes InventoryReservedEvent
3. Payment Service processes payment → publishes PaymentCompletedEvent
4. Order Service confirms order

If any step fails:
→ Compensating transactions execute in reverse order
```

**Choreography vs Orchestration**:
- ShopSphere uses **Event Choreography** (services react to events)
- Alternative: **Orchestration** (central orchestrator manages workflow)

---

### 6. **Adapter Pattern**
**Location**: Feign Clients in Order Service

**Purpose**: Convert interface of a class to another interface

**Implementation**:
```java
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    @PostMapping
    PaymentDTO processPayment(@RequestBody PaymentDTO paymentDTO);
}
```

---

### 7. **Singleton Pattern**
**Location**: Spring Components

**Purpose**: Ensure a class has only one instance

**Implementation**:
- Spring beans are singletons by default
- `@Service`, `@Repository`, `@Component` annotations

---

## Advanced Concepts & Technologies

### 1. **Service Discovery (Eureka)**

**How It Works**:
1. Services register with Eureka on startup
2. Clients query Eureka for service instances
3. Load balancing happens client-side

**Configuration**:
```yaml
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.instance.instance-id=${spring.application.name}:${server.port}
```

**Health Check**:
```yaml
eureka.client.healthcheck.enabled=true
```

---

### 2. **Circuit Breaker Pattern (Resilience4j)**

**Purpose**: Prevent cascading failures

**States**:
- **CLOSED**: Normal operation, requests pass through
- **OPEN**: Failures detected, requests rejected
- **HALF_OPEN**: Testing if service recovered, limited requests pass

**Configuration in User Service**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        slidingWindowSize: 5           # Last 5 calls
        failureRateThreshold: 50       # Fail if 50% fail
        waitDurationInOpenState: 15000 # Wait 15s before half-open
```

**Usage**:
```java
@CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
public UserDTO getUser(String userId) {
    // Service logic
}

public UserDTO getUserFallback(String userId, Exception ex) {
    throw new BusinessException("User service temporarily unavailable");
}
```

---

### 3. **Retry Mechanism (Resilience4j)**

**Purpose**: Automatically retry failed operations

**Configuration**:
```yaml
resilience4j:
  retry:
    instances:
      userService:
        maxAttempts: 3        # Try 3 times
        waitDuration: 1000    # Wait 1 second between retries
```

**Usage**:
```java
@Retry(name = "userService")
public UserDTO createUser(UserDTO userDTO) {
    // Retries automatically
}
```

---

### 4. **Idempotency**

**Problem**: Duplicate requests can cause data inconsistency

**Solution**: Use idempotency keys

**Implementation**:
```java
public OrderDTO createOrder(OrderDTO orderDTO) {
    // Check if order with same idempotencyKey exists
    if (orderRepository.findByIdempotencyKey(orderDTO.getIdempotencyKey()).isPresent()) {
        return existingOrder;
    }
    
    // Create new order
    OrderDTO newOrder = new OrderDTO();
    newOrder.setIdempotencyKey(orderDTO.getIdempotencyKey());
    return save(newOrder);
}
```

**Usage**:
```
POST /orders
Header: Idempotency-Key: unique-key-123
```

---

### 5. **Redis Caching**

**Purpose**: Reduce database load and improve response time

**Implementation in User Service**:
```java
public UserDTO getUser(String userId) {
    UserDTO cachedUser = redisTemplate.opsForValue()
        .get(USER_CACHE_PREFIX + userId);
    
    if (cachedUser != null) {
        return cachedUser;
    }
    
    // Fetch from DB if cache miss
    User user = userRepository.findById(userId).orElseThrow();
    UserDTO result = UserDTOFactory.toDTO(user);
    
    // Cache the result
    redisTemplate.opsForValue().set(
        USER_CACHE_PREFIX + userId,
        result,
        30,  // 30 minutes
        TimeUnit.MINUTES
    );
    
    return result;
}
```

**Cache Invalidation**:
```java
redisTemplate.delete(USER_CACHE_PREFIX + userId);
```

---

### 6. **JWT Authentication + Spring Security**

**JWT Token Structure**:
```
Header.Payload.Signature

Header: {
    "typ": "JWT",
    "alg": "HS512"
}

Payload: {
    "sub": "user-id",
    "email": "user@example.com",
    "iat": 1234567890,
    "exp": 1234571490
}
```

**Token Generation**:
```java
public String generateToken(String userId, String email) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", email);
    
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userId)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
        .compact();
}
```

**Token Validation**:
```java
public Boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

**API Gateway Filter**:
```java
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<Config> {
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = getTokenFromRequest(exchange);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .build();
            }
            return chain.filter(exchange);
        };
    }
}
```

---

### 7. **Event-Driven Communication (Kafka)**

**Architecture**:
- **Publisher**: Services publish events to Kafka topics
- **Consumer**: Other services consume events asynchronously

**Topics in ShopSphere**:
```
1. order-created-topic          → Triggered after order creation
2. payment-completed-topic      → Triggered after payment success
3. inventory-reserved-topic     → Triggered after stock reservation
4. order-cancelled-topic        → Triggered on order cancellation
5. email-sent-topic             → Triggered after email sent
```

**Event Publishing**:
```java
@Service
public class OrderSaga {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void startOrderSaga(OrderDTO orderDTO) {
        KafkaOrderCreatedEvent event = new KafkaOrderCreatedEvent(
            orderDTO.getOrderId(),
            orderDTO.getUserId(),
            orderDTO.getTotalAmount(),
            orderDTO.getIdempotencyKey(),
            System.currentTimeMillis()
        );
        
        kafkaTemplate.send("order-created-topic", event);
    }
}
```

**Event Consumption**:
```java
@Service
public class OrderEventConsumer {
    @KafkaListener(topics = "order-created-topic", groupId = "notification-group")
    public void consumeOrderCreatedEvent(KafkaOrderCreatedEvent event) {
        log.info("Processing order creation for: {}", event.getOrderId());
        // Send email, update status, etc.
    }
}
```

---

### 8. **CompletableFuture & Async Processing**

**Purpose**: Non-blocking asynchronous operations

**Example Usage**:
```java
@Service
public class OrderService {
    @Async
    public CompletableFuture<OrderDTO> createOrderAsync(OrderDTO orderDTO) {
        // Long-running operation
        OrderDTO result = createOrder(orderDTO);
        return CompletableFuture.completedFuture(result);
    }

    public void processMultipleOrders(List<OrderDTO> orders) {
        List<CompletableFuture<OrderDTO>> futures = orders.stream()
            .map(order -> createOrderAsync(order))
            .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );

        allOf.join(); // Wait for all to complete
    }
}
```

---

### 9. **Distributed Tracing (Zipkin)**

**Purpose**: Track requests across multiple services

**Configuration**:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

**Trace Flow**:
```
Client Request
    ↓
API Gateway (Trace-ID: abc123)
    ↓
Order Service (Span: create-order, Trace-ID: abc123)
    ↓
Payment Service (Span: process-payment, Trace-ID: abc123)
    ↓
Response back to Client

Zipkin Dashboard shows entire trace path and latencies
```

**Access Zipkin**: http://localhost:9411

---

### 10. **Centralized Logging (ELK Stack)**

**Architecture**:
```
Services (Logback/SLF4J)
    ↓
Logstash (log aggregation)
    ↓
Elasticsearch (indexing & storage)
    ↓
Kibana (visualization & search)
```

**Service Configuration**:
```yaml
logging:
  level:
    root: INFO
    com.shopsphere: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
```

**Logstash Configuration**:
```
input { tcp/udp on port 5000 }
filter { grok parsing, date processing }
output { send to Elasticsearch }
```

**Access Kibana**: http://localhost:5601

---

### 11. **Prometheus Metrics & Grafana Dashboards**

**Metrics Collected**:
- HTTP request count
- Request latency
- Circuit breaker status
- Database connection pool
- JVM metrics

**Prometheus Configuration**:
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'user-service'
    static_configs:
      - targets: ['localhost:8001']
    metrics_path: '/actuator/prometheus'
```

**Grafana Dashboards**:
- Service Health Status
- Request Latency Distribution
- Error Rates
- Circuit Breaker Events

**Access**:
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

---

### 12. **Rate Limiting**

**Implementation in API Gateway**:
```yaml
spring.cloud.gateway.routes:
  - id: user-service
    uri: lb://user-service
    predicates:
      - Path=/users/**
    filters:
      - name: RequestRateLimiter
        args:
          redis-rate-limiter.replenish-rate: 10        # 10 requests per second
          redis-rate-limiter.burst-capacity: 20        # Allow burst of 20
          redis-rate-limiter.requested-tokens: 1
```

---

### 13. **Database per Service Pattern**

**Benefits**:
- Service independence
- Technology flexibility
- Scalability per service
- Loose coupling

**Databases in ShopSphere**:
```
User Service        → shopsphere_user_db
Product Service     → shopsphere_product_db
Order Service       → shopsphere_order_db
Payment Service     → shopsphere_payment_db
Inventory Service   → shopsphere_inventory_db
Review Service      → shopsphere_review_db
Cart Service        → Redis (no DB)
```

---

### 14. **Load Balancing**

**Client-Side Load Balancing**:
- Spring Cloud LoadBalancer
- Eureka provides service instances
- Round-robin by default

**Usage in Feign Client**:
```java
@FeignClient(name = "payment-service")  // name enables load balancing
public interface PaymentServiceClient {
    @PostMapping("/payments")
    PaymentDTO processPayment(@RequestBody PaymentDTO paymentDTO);
}
```

---

### 15. **CI/CD Pipeline**

**Typical Pipeline Stages**:

```
1. Code Checkout
   └─ Pull code from Git

2. Build
   └─ Maven clean package
   └─ Run unit tests
   └─ Code coverage check

3. SonarQube Analysis
   └─ Code quality check
   └─ Security vulnerabilities scan

4. Docker Image Build
   └─ Build Docker images
   └─ Push to Docker Registry

5. Deploy to Dev
   └─ Deploy to development environment
   └─ Run integration tests

6. Deploy to Staging
   └─ Deploy to staging
   └─ Run smoke tests

7. Deploy to Production
   └─ Manual approval
   └─ Blue-Green deployment
   └─ Smoke tests
   └─ Rollback plan ready
```

**Tools**:
- Git (Version Control)
- Jenkins/GitLab CI (CI/CD)
- Maven (Build)
- Docker (Containerization)
- Kubernetes (Orchestration - optional)

---

## Kafka Events

### Event Types

**1. OrderCreatedEvent**
```json
{
    "orderId": "order-123",
    "userId": "user-456",
    "totalAmount": 1500.50,
    "idempotencyKey": "idem-key-789",
    "timestamp": 1693219200000
}
```
**Consumers**: Inventory Service, Notification Service

**2. PaymentCompletedEvent**
```json
{
    "paymentId": "pay-123",
    "orderId": "order-123",
    "status": "SUCCESS",
    "timestamp": 1693219300000
}
```
**Consumers**: Order Service, Notification Service

**3. InventoryReservedEvent**
```json
{
    "orderId": "order-123",
    "status": "RESERVED",
    "timestamp": 1693219400000
}
```
**Consumers**: Order Service

**4. OrderCancelledEvent**
```
"Order cancelled: order-123"
```
**Consumers**: Inventory Service (release reserved stock), Notification Service

---

## API Documentation

### User Service APIs

```
1. Register User
POST /users/register
Content-Type: application/json

{
    "email": "user@example.com",
    "username": "username123",
    "firstname": "John",
    "lastname": "Doe",
    "phone": "9876543210"
}

Response: 201 Created
{
    "userId": "user-uuid",
    "email": "user@example.com",
    "username": "username123",
    "createdAt": 1693219200000
}

---

2. Login
POST /users/login
?email=user@example.com&password=password

Response: 200 OK
"eyJhbGciOiJIUzUxMiJ9..."

---

3. Get User
GET /users/{userId}
Authorization: Bearer {token}

Response: 200 OK
{
    "userId": "user-uuid",
    "email": "user@example.com",
    ...
}

---

4. Update User
PUT /users/{userId}
Authorization: Bearer {token}
Content-Type: application/json

{
    "firstname": "Jane",
    "phone": "9876543210"
}

Response: 200 OK
```

### Order Service APIs

```
1. Create Order
POST /orders
Authorization: Bearer {token}
Content-Type: application/json
Idempotency-Key: unique-key-123

{
    "userId": "user-uuid",
    "items": [
        {
            "productId": "prod-1",
            "quantity": 2,
            "price": 100.00
        }
    ]
}

Response: 201 Created
{
    "orderId": "order-uuid",
    "status": "PENDING",
    ...
}

---

2. Get Order
GET /orders/{orderId}
Authorization: Bearer {token}

Response: 200 OK
{
    "orderId": "order-uuid",
    "status": "CONFIRMED",
    ...
}

---

3. Cancel Order
PUT /orders/{orderId}/cancel
Authorization: Bearer {token}

Response: 200 OK
```

---

## Running the Project

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0
- Redis 7+
- Kafka 7.5+

### Step 1: Build All Services

```bash
cd E-Commerse-ShopSphere
mvn clean install -DskipTests
```

This builds all modules and creates JAR files.

### Step 2: Build Docker Images

```bash
# Build each service
mvn spring-boot:build-image -DskipTests

# Or use provided Dockerfiles
docker build -t shopsphere/eureka-server:latest ./eureka-server
docker build -t shopsphere/api-gateway:latest ./api-gateway
docker build -t shopsphere/user-service:latest ./user-service
# ... repeat for other services
```

### Step 3: Start Infrastructure

```bash
cd docker-compose
docker-compose up -d
```

This starts:
- MySQL databases (ports 3306-3311)
- Redis (6379)
- Kafka & Zookeeper (9092, 2181)
- Eureka Server (8761)
- Config Server (8888)
- API Gateway (8080)
- All microservices (8001-8008)
- Zipkin (9411)
- Elasticsearch (9200)
- Kibana (5601)
- Prometheus (9090)
- Grafana (3000)

### Step 4: Verify Services

```bash
# Check Eureka
curl http://localhost:8761

# Check API Gateway
curl http://localhost:8080/actuator/health

# Register a user
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "username": "testuser",
    "firstname": "Test",
    "lastname": "User"
  }'
```

### Step 5: Access Dashboards

- **Eureka Dashboard**: http://localhost:8761
- **Kibana**: http://localhost:5601
- **Grafana**: http://localhost:3000 (admin/admin)
- **Zipkin**: http://localhost:9411
- **Prometheus**: http://localhost:9090

### Step 6: Stop Services

```bash
docker-compose down
docker-compose down -v  # Also remove volumes
```

---

## Monitoring & Logging

### Key Metrics to Monitor

1. **Service Health**
   - Circuit breaker status
   - Thread pool utilization
   - Memory usage

2. **Request Metrics**
   - Request count
   - Response time
   - Error rate (4xx, 5xx)

3. **Database Metrics**
   - Connection pool size
   - Query latency
   - Slow queries

4. **Kafka Metrics**
   - Message lag
   - Throughput
   - Consumer group status

### Grafana Dashboards

**JVM Metrics Dashboard**:
- Heap memory usage
- Garbage collection
- Thread count

**Application Performance Dashboard**:
- Request latency (p50, p95, p99)
- Throughput
- Error rate

**Service Dependencies Dashboard**:
- Service-to-service calls
- Circuit breaker events
- Failed requests

### ELK Stack Queries

```
# Find errors in logs
level: "ERROR"

# Find slow queries
duration: > 1000

# Track user actions
"userId: user-123"

# Monitor payment failures
service: "payment-service" AND level: "ERROR"
```

---

## Best Practices

### 1. **API Design**
- Use RESTful principles
- Version APIs: `/v1/`, `/v2/`
- Use consistent error responses
- Document with Swagger/OpenAPI

### 2. **Service Isolation**
- Each service has single responsibility
- Independent databases
- No shared libraries (except DTOs)

### 3. **Error Handling**
- Circuit breaker for resilience
- Retry with exponential backoff
- Meaningful error messages
- Proper HTTP status codes

### 4. **Security**
- JWT for authentication
- Role-based access control (RBAC)
- HTTPS/TLS for communication
- Input validation & sanitization
- Rate limiting

### 5. **Monitoring**
- Log structured data (JSON)
- Use correlation IDs for request tracing
- Alert on critical metrics
- Regular health checks

### 6. **Database**
- Use transactions for consistency
- Index frequently queried fields
- Monitor slow queries
- Regular backups

### 7. **Deployment**
- Blue-green deployment
- Rolling updates
- Health checks before routing traffic
- Automated rollback on failure

### 8. **Testing**
- Unit tests for business logic
- Integration tests for service communication
- Contract testing for API contracts
- Load testing for performance

### 9. **Documentation**
- API documentation
- Service architecture diagrams
- Deployment guides
- Runbooks for incidents

### 10. **Performance**
- Cache frequently accessed data
- Async processing for long operations
- Connection pooling
- Database query optimization

---

## Troubleshooting

### Service Not Registering with Eureka

```
Check:
1. eureka.client.serviceUrl.defaultZone configuration
2. Network connectivity to Eureka server
3. Service name in spring.application.name
4. Spring Cloud version compatibility
```

### Circuit Breaker Always Open

```
Check:
1. failureRateThreshold setting
2. Downstream service availability
3. Network issues
4. Timeout configurations
```

### Kafka Messages Not Being Consumed

```
Check:
1. Topic exists: docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
2. Consumer group status
3. Message serialization/deserialization
4. Consumer offset
```

### Redis Connection Issues

```
Check:
1. Redis service running: docker ps | grep redis
2. Redis port (6379) accessible
3. Password if configured
4. Connection timeout settings
```

---

## Future Enhancements

1. **Kubernetes Deployment**: Deploy using Helm charts
2. **API Gateway Rate Limiting**: Per API endpoint limits
3. **Service Mesh**: Implement with Istio
4. **GraphQL**: Add GraphQL API alongside REST
5. **Multi-tenancy**: Support multiple businesses
6. **Advanced Caching**: Cache aside, write-through patterns
7. **Search**: Elasticsearch integration for product search
8. **Analytics**: Real-time analytics and reporting
9. **Machine Learning**: Recommendation engine
10. **Blockchain**: Supply chain transparency

---

## References

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Microservices Patterns](https://microservices.io/patterns/index.html)
- [12 Factor App](https://12factor.net/)

---

**Author**: ShopSphere Development Team  
**Version**: 1.0.0  
**Last Updated**: June 2024

