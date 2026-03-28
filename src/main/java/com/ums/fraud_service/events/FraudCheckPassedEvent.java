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
public class FraudCheckPassedEvent {

    private String eventId;
    private String eventType;
    private String transactionId;
    private int riskScore;
    private String checkedBy;
    private LocalDateTime timestamp;
}
