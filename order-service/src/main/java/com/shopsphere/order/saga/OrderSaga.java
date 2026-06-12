package com.shopsphere.order.saga;

import com.shopsphere.common.dto.OrderDTO;
import com.shopsphere.common.dto.KafkaOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Saga Pattern Implementation for Order Processing
 * 
 * This demonstrates the Saga pattern which is used for distributed transactions
 * across multiple microservices. The order saga orchestrates the following steps:
 * 1. Order Creation
 * 2. Inventory Reservation
 * 3. Payment Processing
 * 4. Order Confirmation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSaga {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void startOrderSaga(OrderDTO orderDTO) {
        log.info("Starting Order Saga for Order ID: {}", orderDTO.getOrderId());

        // Step 1: Publish OrderCreated event
        KafkaOrderCreatedEvent event = new KafkaOrderCreatedEvent(
            orderDTO.getOrderId(),
            orderDTO.getUserId(),
            orderDTO.getTotalAmount(),
            orderDTO.getIdempotencyKey(),
            System.currentTimeMillis()
        );

        kafkaTemplate.send("order-created-topic", event);
        log.info("OrderCreated event published for Order ID: {}", orderDTO.getOrderId());
    }

    // Compensating Transaction for Order Cancellation
    public void cancelOrderSaga(String orderId) {
        log.info("Cancelling Order Saga for Order ID: {}", orderId);
        kafkaTemplate.send("order-cancelled-topic", "Order cancelled: " + orderId);
    }
}
