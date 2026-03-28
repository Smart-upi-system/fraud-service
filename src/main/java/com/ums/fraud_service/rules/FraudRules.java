package com.ums.fraud_service.rules;

import com.ums.fraud_service.events.TransactionInitiatedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface FraudRules {

    /**
     * Evaluate transaction against this fraud rule
     *
     * @param event Transaction to evaluate
     * @return RuleResult with score and reason
     */
    RuleResult evaluate(TransactionInitiatedEvent event);

    /**
     * Get rule name for logging
     */
    String getRuleName();

    /**
     * Get rule priority (higher = checked first)
     */
    int getPriority();
}

