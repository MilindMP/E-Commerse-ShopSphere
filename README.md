# ShopSphere - E-Commerce Microservices

A production-ready, enterprise-grade e-commerce microservices platform built with Spring Boot and Spring Cloud.

## 🏗️ Architecture Overview

```
Client Applications
        ↓
   API Gateway (8080)
    ↓    ↓    ↓
Services (8001-8008)
    ↓
Infrastructure (Kafka, Redis, MySQL, Eureka)
    ↓
Monitoring (Prometheus, Grafana, Zipkin, ELK)
```

## 📦 Services

| Service | Port | Purpose |
|---------|------|---------|
| Eureka Server | 8761 | Service Registry & Discovery |
| Config Server | 8888 | Configuration Management |
| API Gateway | 8080 | API Gateway & Rate Limiting |
| User Service | 8001 | User Management & Authentication |
| Product Service | 8002 | Product Catalog |
| Cart Service | 8006 | Shopping Cart |
| Order Service | 8003 | Order Management |
| Payment Service | 8004 | Payment Processing |
| Inventory Service | 8005 | Stock Management |
| Notification Service | 8007 | Email & Notifications |
| Review Service | 8008 | Product Reviews |

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Running Locally

1. **Build all services**:
```bash
mvn clean install -DskipTests
```

2. **Start infrastructure**:
```bash
cd docker-compose
docker-compose up -d
```

3. **Register a user**:
```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "testuser",
    "firstname": "Test",
    "lastname": "User"
  }'
```

4. **Access dashboards**:
- Eureka: http://localhost:8761
- Grafana: http://localhost:3000 (admin/admin)
- Kibana: http://localhost:5601
- Zipkin: http://localhost:9411

## 🛠️ Key Technologies

- **Framework**: Spring Boot 3.1.5, Spring Cloud
- **Service Discovery**: Eureka
- **API Gateway**: Spring Cloud Gateway
- **Messaging**: Apache Kafka
- **Cache**: Redis
- **Database**: MySQL (Database per Service)
- **Monitoring**: Prometheus, Grafana
- **Logging**: ELK Stack
- **Tracing**: Zipkin
- **Resilience**: Resilience4j (Circuit Breaker, Retry)
- **Containerization**: Docker, Docker Compose

## 📊 Design Patterns

- **Builder Pattern**: User object construction
- **Factory Pattern**: DTO conversion
- **Strategy Pattern**: Password encoding
- **Saga Pattern**: Distributed transactions
- **Observer Pattern**: Event-driven architecture
- **Adapter Pattern**: Feign clients
- **Singleton Pattern**: Spring beans

## 💡 Key Features

✅ Microservices Architecture
✅ Event-Driven Communication (Kafka)
✅ Service Discovery & Registration (Eureka)
✅ API Gateway with Rate Limiting
✅ JWT Authentication + Spring Security
✅ Circuit Breaker Pattern (Resilience4j)
✅ Retry Mechanism
✅ Idempotency Support
✅ Redis Caching
✅ Distributed Tracing (Zipkin)
✅ Centralized Logging (ELK)
✅ Metrics Collection (Prometheus/Grafana)
✅ Saga Pattern for Distributed Transactions
✅ CompletableFuture for Async Processing
✅ Docker & Docker Compose
✅ Database per Service

## 📚 Documentation

- **[COMPREHENSIVE_DOCUMENTATION.md](./COMPREHENSIVE_DOCUMENTATION.md)** - Complete project documentation with detailed explanations of all concepts

## 🔍 API Examples

### Register User
```bash
POST http://localhost:8080/users/register
Content-Type: application/json

{
    "email": "user@example.com",
    "username": "username",
    "firstname": "John",
    "lastname": "Doe"
}
```

### Login
```bash
POST http://localhost:8080/users/login
?email=user@example.com&password=password
```

### Create Order
```bash
POST http://localhost:8080/orders
Authorization: Bearer {jwt-token}
Content-Type: application/json
Idempotency-Key: unique-key-123

{
    "items": [
        {
            "productId": "prod-1",
            "quantity": 2,
            "price": 100
        }
    ]
}
```

## 📈 Monitoring

### Prometheus Metrics
Access: http://localhost:9090
- Service health metrics
- Request latency
- Error rates
- Circuit breaker status

### Grafana Dashboards
Access: http://localhost:3000
- JVM metrics
- Application performance
- Service dependencies

### Kibana Logs
Access: http://localhost:5601
- Centralized logging
- Log search and analysis
- Alert configuration

### Zipkin Traces
Access: http://localhost:9411
- Distributed tracing
- Service call flow
- Latency analysis

## 🐳 Docker Compose

Includes:
- MySQL databases (per service)
- Redis
- Apache Kafka + Zookeeper
- Elasticsearch + Kibana + Logstash
- Prometheus + Grafana
- Zipkin

## 🔄 Event Flow

```
1. User creates order
   └─> Order Service publishes OrderCreatedEvent

2. Inventory Service consumes OrderCreatedEvent
   └─> Reserves stock
   └─> Publishes InventoryReservedEvent

3. Payment Service consumes OrderCreatedEvent
   └─> Processes payment
   └─> Publishes PaymentCompletedEvent

4. Order Service consumes PaymentCompletedEvent
   └─> Confirms order

5. Notification Service consumes all events
   └─> Sends email notifications
```

## 📝 Project Structure

```
E-Commerse-ShopSphere/
├── eureka-server/              # Service Registry
├── config-server/              # Configuration Server
├── api-gateway/                # API Gateway
├── user-service/               # User Management
├── product-service/            # Product Catalog
├── cart-service/               # Shopping Cart
├── order-service/              # Order Management
├── payment-service/            # Payment Processing
├── inventory-service/          # Inventory Management
├── notification-service/       # Notifications
├── review-service/             # Product Reviews
├── common-lib/                 # Shared DTOs & Utils
├── docker-compose/             # Docker Compose Configuration
├── COMPREHENSIVE_DOCUMENTATION.md  # Full documentation
├── README.md                   # This file
└── pom.xml                     # Parent POM
```

## 🛡️ Security

- JWT-based authentication
- Role-based access control ready
- Spring Security integration
- Encrypted passwords (BCrypt)
- Request validation
- Rate limiting per user

## 🔧 Configuration

All services are configured via:
1. **application.yml** - Default configuration
2. **Config Server** - Centralized configuration
3. **Environment variables** - Docker Compose overrides

## ⚙️ Resilience Features

- **Circuit Breaker**: Prevents cascading failures
- **Retry**: Automatic retry with exponential backoff
- **Timeout**: Request timeouts
- **Rate Limiting**: Per-user request limits
- **Fallback**: Graceful degradation

## 📊 Database Schema

Each service has its own database:
- **User DB**: Users table
- **Product DB**: Products table  
- **Order DB**: Orders, OrderItems tables
- **Payment DB**: Payments table
- **Inventory DB**: Inventory, Reservations tables
- **Review DB**: Reviews table

## 🚨 Troubleshooting

### Services not registering
- Check Eureka dashboard
- Verify network connectivity
- Check service configuration

### Kafka messages not consumed
- Check topic creation
- Verify consumer group status
- Check message serialization

### High latency
- Check circuit breaker status
- Monitor database connections
- Check Kafka lag

## 📖 Additional Resources

- [Microservices Patterns](https://microservices.io/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Kafka Best Practices](https://kafka.apache.org/)
- [12 Factor App](https://12factor.net/)

## 🤝 Contributing

1. Create a feature branch
2. Make changes
3. Add tests
4. Submit pull request

## 📄 License

MIT License

## 👨‍💻 Author

ShopSphere Development Team

---

For detailed documentation, see [COMPREHENSIVE_DOCUMENTATION.md](./COMPREHENSIVE_DOCUMENTATION.md)
