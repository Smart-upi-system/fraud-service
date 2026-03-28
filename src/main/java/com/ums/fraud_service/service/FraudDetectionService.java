package com.ums.fraud_service.service;

import com.ums.fraud_service.events.TransactionInitiatedEvent;
import com.ums.fraud_service.model.dto.FraudCheckResult;

public interface FraudDetectionService {
    /**
     * Analyze transaction for fraud
     *
     * @param event Transaction to analyze
     * @return FraudCheckResult with decision and score
     */
    FraudCheckResult analyzeTransaction(TransactionInitiatedEvent event);
}
