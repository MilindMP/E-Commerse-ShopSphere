package com.shopsphere.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaInventoryReservedEvent implements Serializable {
    private String orderId;
    private String status;
    private Long timestamp;
}
