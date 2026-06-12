package com.shopsphere.notification.consumer;

import com.shopsphere.common.dto.KafkaOrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEventConsumer {
    
    @KafkaListener(topics = "order-created-topic", groupId = "notification-group")
    public void consumeOrderCreatedEvent(KafkaOrderCreatedEvent event) {
        log.info("Order Created Event received: Order ID: {}", event.getOrderId());
        // Send email notification
        sendOrderConfirmationEmail(event.getUserId(), event.getOrderId());
    }

    @KafkaListener(topics = "payment-completed-topic", groupId = "notification-group")
    public void consumePaymentCompletedEvent(String message) {
        log.info("Payment Completed Event: {}", message);
        // Send payment confirmation email
    }

    private void sendOrderConfirmationEmail(String userId, String orderId) {
        log.info("Sending order confirmation email to user: {} for order: {}", userId, orderId);
        // Implement email sending logic
    }
}
