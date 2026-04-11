package com.ums.fraud_service.events.consumer;

import com.ums.fraud_service.events.TransactionInitiatedEvent;
import com.ums.fraud_service.events.producer.FraudEventProducer;
import com.ums.fraud_service.model.dto.FraudCheckResult;
import com.ums.fraud_service.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableKafka
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final FraudEventProducer fraudEventProducer;

    @KafkaListener(
            topics = "${spring.kafka.topics.fraud-requests:fraud.requests}",
            groupId = "${spring.kafka.consumer.fraud.group-id:transaction-service-fraud-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransactionInitiated(TransactionInitiatedEvent event) {
        // 1. CRITICAL: Filter to prevent infinite loop
        // We only want to process events where a transaction is actually being initiated.
        // Based on your logs, your producer sends 'FraudCheckPassed' or 'FraudCheckFailed'.
        // We must ignore those types here.
        if (event.getEventType() == null ||
                !event.getEventType().equals("FraudCheckRequested")) {
            log.debug("Skipping event type: {}", event.getEventType());
            return;
        }

        // 2. Validate essential data
        if (event.getTransactionId() == null) {
            log.error("Received event with missing transactionId. Payload: {}", event);
            return;
        }

        log.info("Received TransactionInitiated event: transactionId={}, amount={}",
                event.getTransactionId(), event.getAmount());

        try {
            FraudCheckResult result = fraudDetectionService.analyzeTransaction(event);

            if (result == null) {
                log.warn("Fraud check returned no result (likely already processed): transactionId={}",
                        event.getTransactionId());
                return;
            }

            if (result.isFraudDetected()) {
                log.warn("FRAUD DETECTED: transactionId={}, riskScore={}, reason={}",
                        result.getTransactionId(), result.getRiskScore(), result.getReason());

                fraudEventProducer.publishFraudCheckFailed(
                        event.getTransactionId(),
                        result.getReason(),
                        result.getRiskScore()
                );
            } else {
                log.info("Transaction passed fraud check: transactionId={}, riskScore={}",
                        result.getTransactionId(), result.getRiskScore());

                fraudEventProducer.publishFraudCheckPass(
                        event.getTransactionId(),
                        result.getRiskScore(),
                        result.getCheckedBy()
                );
            }

        } catch (Exception e) {
            // Using a generic Exception catch here to ensure we don't crash the listener
            // and to handle the 'Already Analyzed' scenario if your service throws it.
            log.error("Error processing transaction for fraud check: transactionId={}. Error: {}",
                    event.getTransactionId(), e.getMessage());

            // Optional: Only publish failure if it's a real logic error, not a duplicate check
            if (!e.getMessage().contains("already analyzed")) {
                fraudEventProducer.publishFraudCheckFailed(
                        event.getTransactionId(),
                        "Fraud check system error: " + e.getMessage(),
                        100
                );
            }
        }
    }
}