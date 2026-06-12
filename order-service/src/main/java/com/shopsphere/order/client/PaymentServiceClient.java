package com.shopsphere.order.client;

import com.shopsphere.common.dto.PaymentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Synchronous Communication via Feign Client
@FeignClient(name = "payment-service", path = "/payments")
public interface PaymentServiceClient {
    @PostMapping
    PaymentDTO processPayment(@RequestBody PaymentDTO paymentDTO);
}
