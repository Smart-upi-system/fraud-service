package com.ums.fraud_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckFailedEvent {

    private String eventId;
    private String eventType; // "FraudCheckFailed"
    private String transactionId;
    private String reason;
    private int riskScore; // 0-100
    private LocalDateTime timestamp;
}
