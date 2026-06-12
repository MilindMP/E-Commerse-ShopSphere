# ShopSphere Architecture Guide

## System Architecture

### 1. Overall System Design

```
┌─────────────────────────────────────────────────────────────────┐
│                      Client Layer                                │
│  (Web Browser, Mobile App, Third-party Services)                 │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
            ┌──────────────────────────────────┐
            │       API Gateway                 │
            │     (Port 8080)                   │
            │  - JWT Validation                 │
            │  - Rate Limiting                  │
            │  - Request Routing                │
            └──────────────────┬───────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
    ┌─────────────┐    ┌─────────────┐      ┌─────────────────┐
    │   Eureka    │    │   Config    │      │  Microservices  │
    │   Server    │    │   Server    │      │  (8001-8008)    │
    │  (8761)     │    │  (8888)     │      │                 │
    └─────────────┘    └─────────────┘      └─────────────────┘
         │                                          │
         │                      ┌───────────────────┼──────────────┐
         │                      │                   │              │
         ▼                      ▼                   ▼              ▼
     ┌────────────────┐  ┌────────────┐  ┌─────────────┐  ┌──────────┐
     │  Service       │  │   Data     │  │  Message   │  │  Cache   │
     │  Discovery     │  │  Storage   │  │  Broker    │  │ Storage  │
     │  (Consul/     │  │  (MySQL)   │  │  (Kafka)   │  │ (Redis)  │
     │   Eureka)      │  └────────────┘  └─────────────┘  └──────────┘
     └────────────────┘
```

### 2. Service Communication Pattern

#### Synchronous Communication
```
Client Request
    ↓
API Gateway
    ↓
Order Service (Feign Client)
    ↓ (REST call via Load Balancer)
Payment Service
    ↓
Response back
```

**Advantages**:
- Real-time response
- Immediate error detection
- Direct dependency tracking

**Disadvantages**:
- Tight coupling
- Cascading failures
- Performance degradation

#### Asynchronous Communication
```
Order Service publishes OrderCreatedEvent
    ↓
Kafka Topic (order-created-topic)
    ↓ (Independent consumers)
├─→ Inventory Service (consume & reserve stock)
├─→ Payment Service (consume & process payment)
└─→ Notification Service (consume & send email)

Each consumer processes independently
No blocking, improved resilience
```

**Advantages**:
- Loose coupling
- Better resilience
- Higher throughput

**Disadvantages**:
- Eventual consistency
- Difficult debugging
- Potential message loss

### 3. Saga Pattern for Distributed Transactions

#### Flow Diagram
```
Step 1: Order Service creates order
        ↓
        Publishes: OrderCreatedEvent
        Status: PENDING
        
Step 2: Inventory Service consumes OrderCreatedEvent
        ↓
        Attempts: Reserve stock
        ├─ Success: Publishes InventoryReservedEvent
        └─ Failure: Publishes OrderCancelledEvent (compensation)
        
Step 3: Payment Service consumes OrderCreatedEvent
        ↓
        Attempts: Process payment
        ├─ Success: Publishes PaymentCompletedEvent
        └─ Failure: Publishes OrderCancelledEvent (compensation)
        
Step 4: Order Service consumes both InventoryReservedEvent & PaymentCompletedEvent
        ├─ Both successful: Updates order to CONFIRMED
        └─ Either failed: Triggers compensation (stock release, payment refund)
```

**Compensation Transactions**:
```
If payment fails:
  Inventory Service → Release reserved stock

If inventory fails:
  Payment Service → Refund payment

Maintains data consistency across services
```

### 4. Data Flow - Order Creation

```
1. User submits order request to API Gateway
   POST /orders
   Authorization: Bearer {jwt_token}
   Idempotency-Key: unique-key-123
   
2. API Gateway
   - Validates JWT token
   - Extracts user ID from token
   - Routes to Order Service
   - Checks rate limit (10 req/sec, burst 20)

3. Order Service
   - Validates idempotency key (prevents duplicates)
   - Creates order in database (status: PENDING)
   - Creates Order Saga orchestrator
   - Publishes OrderCreatedEvent to Kafka

4. Kafka Message Distribution
   Topic: order-created-topic
   ├─ Partition 0: Message sent to consumers
   └─ Replicas: Backup for durability

5. Consumer Processing (Parallel)
   
   Inventory Service (Consumer Group: inventory-group)
   - Consumes OrderCreatedEvent
   - Validates product availability
   - Reserves stock in database
   - Publishes InventoryReservedEvent
   
   Payment Service (Consumer Group: payment-group)
   - Consumes OrderCreatedEvent
   - Validates payment method
   - Processes payment (calls external gateway)
   - Publishes PaymentCompletedEvent
   
   Notification Service (Consumer Group: notification-group)
   - Consumes OrderCreatedEvent
   - Sends order confirmation email
   - Logs notification sent

6. Order Service (Saga Orchestrator)
   - Consumes InventoryReservedEvent
   - Consumes PaymentCompletedEvent
   - If both successful:
     └─ Updates order status to CONFIRMED
     └─ Publishes OrderConfirmedEvent
   - If either failed:
     └─ Publishes OrderCancelledEvent
     └─ Triggers compensation transactions

7. Final Response
   - Order confirmed or failed
   - Client receives response
   - Notification sent to user
```

### 5. Caching Strategy

```
User Requests Data
    ↓
Check Redis Cache
    ├─ Cache HIT: Return cached data (90% of requests)
    ├─ Cache MISS: Query database
    │   ├─ Store in Redis (TTL: 30 mins)
    │   └─ Return to client
    └─ Cache INVALIDATION on data update
       Delete from cache, next request loads fresh data
```

**Cache Patterns**:
- **Cache-Aside**: App manages cache
- **TTL**: Automatic expiration (30 minutes)
- **Invalidation**: On updates, deletes cache

### 6. Circuit Breaker States

```
CLOSED (Normal)
  ├─ Requests pass through
  ├─ Failures counted (sliding window: 5 calls)
  └─ If failure rate > 50% → OPEN

OPEN (Failing)
  ├─ Requests rejected immediately (fail fast)
  ├─ Fallback method called
  └─ Wait 15 seconds → HALF_OPEN

HALF_OPEN (Testing)
  ├─ Limited requests allowed (2 calls)
  ├─ If successful → CLOSED
  └─ If failed → OPEN
```

**Example**:
```
Service call attempt
    ↓
Circuit Breaker checks state
├─ CLOSED: Call goes through
├─ OPEN: Rejected, fallback called
└─ HALF_OPEN: Limited call allowed
```

### 7. Security Architecture

#### JWT Token Flow
```
User Credentials
    ↓
User Service
    ├─ Validate credentials
    ├─ Hash password check (BCrypt)
    └─ Generate JWT (HS512 algorithm)
    
JWT Token Structure:
┌─────────────────────────────────┐
│ Header: alg, typ                │
├─────────────────────────────────┤
│ Payload: userId, email, exp     │
├─────────────────────────────────┤
│ Signature: HMAC-SHA512          │
└─────────────────────────────────┘

Client includes JWT in requests
    ↓
API Gateway
    ├─ Extract JWT from Authorization header
    ├─ Validate signature
    ├─ Check expiration
    └─ Extract userId, add to header (X-User-Id)
    
Downstream services
    ├─ Receive userId from gateway
    ├─ Trust the gateway validation
    └─ Process request with user context
```

### 8. Monitoring & Observability Stack

```
Application Services
    ├─ Generate logs (Logback/SLF4J)
    ├─ Generate metrics (Micrometer)
    └─ Generate traces (Spring Cloud Sleuth + Brave)

Logs Flow:
Services → Logstash → Elasticsearch → Kibana
           (parse)     (index)         (visualize)

Metrics Flow:
Services → Prometheus → Grafana
           (scrape)     (dashboard)

Traces Flow:
Services → Zipkin
           (trace aggregation)
           (latency analysis)
```

### 9. Service Discovery with Eureka

```
Service Startup
    ↓
Register with Eureka
├─ POST /eureka/apps/{serviceName}
├─ Include instance details (host, port, status)
└─ Set heartbeat interval (30 seconds)

Service Discovery
Client needs Payment Service
    ↓
Query Eureka
└─ GET /eureka/apps/payment-service
└─ Returns list of instances
└─ Client-side load balancing (round-robin)
└─ Call Payment Service

Heartbeat & Health Checks
└─ Every 30 seconds, send heartbeat
└─ If no heartbeat after 90 seconds → mark unavailable
└─ If no heartbeat after 180 seconds → remove from registry
```

### 10. Event Sourcing Pattern

```
Event Log (Immutable)
┌─────────────────────────────────────┐
│ 1. OrderCreatedEvent(orderId, ...)  │
│ 2. PaymentProcessedEvent(...)       │
│ 3. InventoryReservedEvent(...)      │
│ 4. OrderConfirmedEvent(...)         │
└─────────────────────────────────────┘
         ↓
Current State (Derived)
┌─────────────────────────────────┐
│ Order: CONFIRMED                │
│ Payment: COMPLETED              │
│ Inventory: RESERVED             │
└─────────────────────────────────┘

Benefits:
- Complete audit trail
- Time travel (replay events)
- Event analytics
```

### 11. Idempotency Implementation

```
Client sends request with Idempotency-Key
┌──────────────────────────────────┐
│ POST /orders                     │
│ Idempotency-Key: uuid-123        │
├──────────────────────────────────┤
│ { order data }                   │
└──────────────────────────────────┘
         ↓
Server checks database
├─ Key exists? → Return stored response (don't reprocess)
└─ Key new? → Process request
         ├─ Store request & response
         ├─ Associate with key
         └─ Return response

Benefits:
- Duplicate requests safe
- No double charges
- Improved user experience
```

### 12. Database Design Pattern: Database per Service

```
Microservices Architecture
┌─────────────────────────────────────────┐
│  Service 1  │  Service 2  │  Service 3  │
└──────┬──────┴──────┬───────┴──────┬─────┘
       ↓             ↓              ↓
     DB1           DB2            DB3
  (MySQL)        (MySQL)        (MySQL)
  
Schema 1        Schema 2        Schema 3
- Users         - Products      - Orders
- Profiles      - Categories    - OrderItems
- Roles         - Inventory     - OrderStatus

Benefits:
✓ Schema flexibility per service
✓ Technology choice freedom
✓ Independent scaling
✓ Reduced coupling

Challenges:
✗ Cross-service joins
✗ Distributed transactions (solved by Saga)
✗ Data consistency (eventual consistency)
```

### 13. Deployment Architecture

```
Development
├─ Local machine
├─ In-memory H2 database
└─ Mock Kafka/Redis

Staging
├─ Docker Compose locally
├─ MySQL containers
├─ Redis/Kafka containers
└─ Full integration testing

Production
├─ Kubernetes cluster
├─ Managed databases (AWS RDS, Azure)
├─ Managed message broker (AWS MSK, Azure Event Hubs)
├─ Auto-scaling groups
├─ Multi-zone deployment
├─ CI/CD pipeline
└─ Blue-Green deployment

CI/CD Pipeline:
Git Push → Build → Test → SonarQube → Docker Build
    ↓
Push to Registry → Deploy to Dev → Deploy to Staging
    ↓
Manual Approval → Blue-Green Deploy Prod
    ↓
Smoke Tests → Traffic Switch → Monitoring
```

## Performance Considerations

### Optimization Strategies

1. **Database Level**
   - Connection pooling (HikariCP)
   - Query optimization
   - Indexing on frequently searched fields
   - Read replicas for read-heavy services

2. **Application Level**
   - Caching (Redis)
   - Async processing (CompletableFuture)
   - Batch processing for bulk operations
   - Request/Response compression

3. **Infrastructure Level**
   - Load balancing
   - Auto-scaling based on metrics
   - CDN for static content
   - Database sharding (horizontal scaling)

## Scalability Patterns

### Horizontal Scaling
```
Load Balancer
├─ User Service Instance 1
├─ User Service Instance 2
├─ User Service Instance 3
└─ User Service Instance N

Each instance:
- Registers with Eureka
- Connects to shared database
- Caches locally in Redis
```

### Vertical Scaling
```
Increase JVM memory: -Xmx2g -Xms1g
Increase thread pool size
Upgrade database resources
```

## Disaster Recovery

### Backup Strategy
```
Daily Database Backups
├─ MySQL snapshots
├─ Stored in S3/Backup service
└─ Retention: 30 days

Event Log Archival
├─ Kafka logs backed up
├─ Elasticsearch snapshots
└─ Full audit trail

Configuration Backups
├─ Config Server repository
└─ Version control (Git)
```

### Recovery Process
```
1. Detect failure
2. Alert team
3. Failover to backup
4. Restore from latest snapshot
5. Replay events from event log
6. Verify data consistency
7. Return to normal operation
```

---

This architecture provides:
- ✅ High availability
- ✅ Scalability
- ✅ Maintainability
- ✅ Observability
- ✅ Resilience
- ✅ Security
