package com.ums.fraud_service.rules;

import com.ums.fraud_service.events.TransactionInitiatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class AmountThresholdRule implements FraudRules {

    @Value("${fraud.rules.amount-threshold:50000}")
    private BigDecimal highAmountThreshold;

    @Override
    public RuleResult evaluate(TransactionInitiatedEvent event) {
        log.debug("Evaluating AmountThresholdRule: amount={}, threshold={}",
                event.getAmount(), highAmountThreshold);

        if (event.getAmount().compareTo(highAmountThreshold) > 0) {
            return RuleResult.builder()
                    .triggered(true)
                    .scoreContribution(40) // High risk
                    .reason(String.format("Amount ₹%s exceeds threshold ₹%s",
                            event.getAmount(), highAmountThreshold))
                    .build();
        }

        return RuleResult.builder()
                .triggered(false)
                .scoreContribution(0)
                .build();
    }

    @Override
    public String getRuleName() {
        return "AMOUNT_THRESHOLD";
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
