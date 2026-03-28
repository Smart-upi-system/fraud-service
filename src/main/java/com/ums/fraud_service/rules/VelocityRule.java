package com.ums.fraud_service.rules;

import com.ums.fraud_service.events.TransactionInitiatedEvent;
import com.ums.fraud_service.repository.FraudCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class VelocityRule  implements FraudRules {

    private final FraudCheckRepository fraudCheckRepository;

    @Value("${fraud.rules.velocity-limit:5}")
    private int maxTransactionsPerMinute;

    @Override
    public RuleResult evaluate(TransactionInitiatedEvent event) {
        log.debug("Evaluating VelocityRule: senderId={}", event.getSenderId());

        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long recentCount = fraudCheckRepository.countTransactionsBySenderSince(
                event.getSenderId(), oneMinuteAgo);

        if (recentCount >= maxTransactionsPerMinute) {
            return RuleResult.builder()
                    .triggered(true)
                    .scoreContribution(50) // Very high risk
                    .reason(String.format("Velocity limit exceeded: %d transactions in 1 minute (limit: %d)",
                            recentCount, maxTransactionsPerMinute))
                    .build();
        }

        return RuleResult.builder()
                .triggered(false)
                .scoreContribution(0)
                .build();
    }

    @Override
    public String getRuleName() {
        return "VELOCITY";
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
