package com.ums.fraud_service.events.producer;


import com.ums.fraud_service.events.FraudCheckFailedEvent;
import com.ums.fraud_service.events.FraudCheckPassedEvent;
import com.ums.fraud_service.model.dto.FraudCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.fraud-results:fraud.results}")
    private String fraudEventsTopic;



    /**
     * Publish FraudCheckFailed event
     */
    public void publishFraudCheckFailed(UUID transactionId, String reason, int riskScore) {
        log.info("FraudEventProducer: publishFraudCheckFailed(): Publishing FraudCheckFailed: transactionId={}, reason={}, checkedBy={}",
                transactionId, riskScore,reason);

        FraudCheckFailedEvent event=FraudCheckFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FraudCheckFailed")
                .transactionId(transactionId)
                .riskScore(riskScore)
                .timestamp(LocalDateTime.now())
                .reason(reason)
                .build();

        kafkaTemplate.send(fraudEventsTopic,transactionId.toString(),event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.warn("FraudCheckFailed event published: transactionId={}", transactionId);
                    } else {
                        log.error("Failed to publish FraudCheckFailed: transactionId={}",
                                transactionId, ex);
                    }
                });

    }

    /**
     * Publish FraudCheckPassed event
     */
    public void publishFraudCheckPass(UUID transactionId, int riskScore, String checkedBy) {
        log.info("FraudEventProducer: publishFraudCheckPass(): Publishing FraudCheckPassed: transactionId={}, riskScore={}, checkedBy={}",
                transactionId, riskScore,checkedBy);


        FraudCheckPassedEvent event = FraudCheckPassedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FraudCheckPassed")
                .transactionId(transactionId)
                .riskScore(riskScore)
                .checkedBy(checkedBy)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(fraudEventsTopic,transactionId.toString(),event)
                .whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("FraudCheckPassed event published: transactionId={}", transactionId);
            } else {
                log.error("Failed to publish FraudCheckPassed: transactionId={}",
                        transactionId, ex);
            }
        });



    }
}
