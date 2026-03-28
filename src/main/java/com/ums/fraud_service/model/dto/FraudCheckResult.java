package com.ums.fraud_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudCheckResult {
    private UUID transactionId;
    private boolean fraudDetected;
    private int riskScore; // 0-100
    private String reason;
    private String checkedBy;
}
