package com.shopsphere.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO implements Serializable {
    private String paymentId;
    private String orderId;
    private String userId;
    private java.math.BigDecimal amount;
    private String status; // PENDING, SUCCESS, FAILED
    private String method; // CREDIT_CARD, DEBIT_CARD, UPI, WALLET
    private Long createdAt;
}
