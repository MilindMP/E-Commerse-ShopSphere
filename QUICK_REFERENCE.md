# ShopSphere Quick Reference Guide

## Service Ports
- API Gateway: 8080
- User Service: 8001
- Product Service: 8002
- Order Service: 8003
- Payment Service: 8004
- Inventory Service: 8005
- Cart Service: 8006
- Notification Service: 8007
- Review Service: 8008
- Eureka Server: 8761
- Config Server: 8888

## Infrastructure Ports
- MySQL: 3306 (various databases on different ports)
- Redis: 6379
- Kafka: 9092
- Zookeeper: 2181
- Elasticsearch: 9200
- Kibana: 5601
- Prometheus: 9090
- Grafana: 3000
- Zipkin: 9411

## Common Commands

### Build
```bash
mvn clean install -DskipTests
mvn clean package
mvn -DskipTests clean install
```

### Run Individual Service
```bash
cd user-service
mvn spring-boot:run
```

### Docker Commands
```bash
# Build images
docker build -t shopsphere/user-service:latest ./user-service

# Run container
docker run -p 8001:8001 shopsphere/user-service:latest

# View logs
docker logs container-name
docker logs -f container-name  # Follow logs
```

### Docker Compose
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild images
docker-compose build

# Remove volumes
docker-compose down -v
```

### Maven Profiles
```bash
# Development
mvn clean install -Pdev

# Production
mvn clean install -Pprod
```

## Authentication Flow

1. **Register User**
   ```
   POST /users/register
   ```

2. **Login**
   ```
   POST /users/login?email=...&password=...
   Response: JWT Token
   ```

3. **Use Token**
   ```
   GET /orders
   Authorization: Bearer {token}
   ```

## Kafka Topics
- `order-created-topic` → Order Service publishes
- `payment-completed-topic` → Payment Service publishes
- `inventory-reserved-topic` → Inventory Service publishes
- `order-cancelled-topic` → Order Service publishes
- `email-sent-topic` → Notification Service publishes

## Database Connection Strings

| Service | Connection String |
|---------|-------------------|
| User | jdbc:mysql://localhost:3306/shopsphere_user_db |
| Product | jdbc:mysql://localhost:3307/shopsphere_product_db |
| Order | jdbc:mysql://localhost:3308/shopsphere_order_db |
| Payment | jdbc:mysql://localhost:3309/shopsphere_payment_db |
| Inventory | jdbc:mysql://localhost:3310/shopsphere_inventory_db |
| Review | jdbc:mysql://localhost:3311/shopsphere_review_db |

All: username=root, password=root

## Useful URLs

| Service | URL |
|---------|-----|
| Eureka | http://localhost:8761 |
| Grafana | http://localhost:3000 (admin/admin) |
| Kibana | http://localhost:5601 |
| Prometheus | http://localhost:9090 |
| Zipkin | http://localhost:9411 |
| Swagger (if enabled) | http://localhost:8080/swagger-ui.html |

## Debugging

### Check Service Health
```bash
curl http://localhost:8001/actuator/health
```

### Check Eureka Registration
```bash
curl http://localhost:8761/eureka/apps
```

### Kafka List Topics
```bash
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Check Redis
```bash
docker exec shopsphere_redis redis-cli
> PING
> KEYS *
```

## Performance Tuning

### JVM Settings
```bash
-Xmx512m -Xms512m    # Min/Max heap
-XX:+UseG1GC         # G1 garbage collector
-XX:MaxGCPauseMillis=200
```

### Connection Pooling
```yaml
spring.datasource.hikari.maximum-pool-size: 20
spring.datasource.hikari.minimum-idle: 5
```

### Kafka Performance
```yaml
spring.kafka.producer.batch-size: 16384
spring.kafka.producer.linger-ms: 10
spring.kafka.consumer.fetch-max-bytes: 1048576
```

## Logging Levels

```yaml
logging:
  level:
    root: INFO
    com.shopsphere: DEBUG
    org.springframework.web: DEBUG
    org.springframework.cloud: DEBUG
```

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Service not registering | Check Eureka config, verify network |
| Circuit breaker open | Check downstream service health |
| Kafka messages lost | Check broker config, replication factor |
| High latency | Check database indexes, connection pool |
| Out of memory | Increase JVM heap size |

## Environment Variables

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
SPRING_REDIS_HOST=redis
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## Circuit Breaker States

- **CLOSED**: Normal, requests pass through
- **OPEN**: Failures detected, requests rejected, fail fast
- **HALF_OPEN**: Testing recovery, limited requests allowed

## Rate Limiting

Default: 10 requests/second per user
Burst capacity: 20 requests

Configure in API Gateway config:
```yaml
redis-rate-limiter.replenish-rate: 10
redis-rate-limiter.burst-capacity: 20
```

## JWT Token Structure

```
Header: {
  "alg": "HS512",
  "typ": "JWT"
}

Payload: {
  "sub": "user-id",
  "email": "user@example.com",
  "iat": 1234567890,
  "exp": 1234571490
}

Signature: HMACSHA512(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
```

### With Code Coverage
```bash
mvn clean test jacoco:report
```

## Deployment Checklist

- [ ] All services built and tested
- [ ] Docker images created
- [ ] docker-compose.yml configured
- [ ] Databases initialized
- [ ] Eureka server running
- [ ] Config Server running
- [ ] API Gateway running
- [ ] All microservices running
- [ ] Health checks passing
- [ ] Monitoring configured
- [ ] Logging working
- [ ] Backup strategy implemented
- [ ] Rollback plan ready

---

For more information, refer to [COMPREHENSIVE_DOCUMENTATION.md](./COMPREHENSIVE_DOCUMENTATION.md)
