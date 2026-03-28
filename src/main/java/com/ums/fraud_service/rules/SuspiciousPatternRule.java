package com.ums.fraud_service.rules;

import com.ums.fraud_service.events.TransactionInitiatedEvent;
import com.ums.fraud_service.repository.FraudCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuspiciousPatternRule implements FraudRules{

    private final FraudCheckRepository fraudCheckRepository;

    @Override
    public RuleResult evaluate(TransactionInitiatedEvent event) {
        log.debug("Evaluating SuspiciousPatternRule: senderId={}", event.getSenderId());

        LocalDateTime oneDayAgo=LocalDateTime.now().minusDays(1);
        long previousFraudCount=fraudCheckRepository.countFraudAttemptsBySenderSince(event.getSenderId(),oneDayAgo);

        if(previousFraudCount>0){
            return RuleResult.builder()
                    .triggered(true)
                    .scoreContribution(60) // Very high risk
                    .reason(String.format("User has %d fraud attempts in last 24 hours",
                            previousFraudCount))
                    .build();
        }

        // Check for round number amounts (often used in fraud)
        if (event.getAmount().remainder(new java.math.BigDecimal("1000")).compareTo(java.math.BigDecimal.ZERO) == 0
                && event.getAmount().compareTo(new java.math.BigDecimal("5000")) >= 0) {
            return RuleResult.builder()
                    .triggered(true)
                    .scoreContribution(15) // Low-medium risk
                    .reason("Suspicious round number amount: ₹" + event.getAmount())
                    .build();
        }


        return RuleResult.builder()
                .triggered(false)
                .scoreContribution(0)
                .build();
    }

    @Override
    public String getRuleName() {
        return "SuspiciousPatternRule";
    }

    @Override
    public int getPriority() {
        return 4;
    }
}
