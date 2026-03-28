package com.ums.fraud_service.rules;

import com.ums.fraud_service.events.TransactionInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewReceiverRule implements FraudRules {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RECEIVER_KEY_PREFIX = "fraud:receivers:";
    private static final Duration RECEIVER_TTL = Duration.ofDays(30);

    @Override
    public RuleResult evaluate(TransactionInitiatedEvent event) {
        log.debug("Evaluating NewReceiverRule: senderId={}, receiverId={}",
                event.getSenderId(), event.getReceiverId());

        String key = RECEIVER_KEY_PREFIX + event.getSenderId() + ":" + event.getReceiverId();
        Boolean hasInteractedBefore = redisTemplate.hasKey(key);

        if (Boolean.FALSE.equals(hasInteractedBefore)) {
            // First time sender is sending to this receiver
            // Mark as interacted for future checks
            redisTemplate.opsForValue().set(key, "1", RECEIVER_TTL);

            return RuleResult.builder()
                    .triggered(true)
                    .scoreContribution(20) // Medium risk
                    .reason("First transaction to this receiver")
                    .build();
        }

        return RuleResult.builder()
                .triggered(false)
                .scoreContribution(0)
                .build();
    }

    @Override
    public String getRuleName() {
        return "NewReceiverRule";
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
