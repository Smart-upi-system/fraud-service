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
            topics = "${spring.kafka.topics.fraud-events:fraud.events}",
            groupId = "${spring.kafka.consumer.fraud.group-id:transaction-service-fraud-group}",
            containerFactory = "KafkaListenerFactory"
    )
    public void handleTransactionInitiated(TransactionInitiatedEvent event){
        log.info("Received TransactionInitiated event: transactionId={}, amount={}",
                event.getTransactionId(), event.getAmount());

            try{

                FraudCheckResult result=fraudDetectionService.analyzeTransaction(event);
                if(result.isFraudDetected()){
                    log.warn("FRAUD DETECTED: transactionId={}, riskScore={}, reason={}",
                            result.getTransactionId(), result.getRiskScore(), result.getReason());

                    fraudEventProducer.publishFraudCheckFailed(event.getTransactionId(),result.getReason(),result.getRiskScore());
                }
                else{

                    log.info("Transaction passed fraud check: transactionId={}, riskScore={}",
                            result.getTransactionId(), result.getRiskScore());
                    fraudEventProducer.publishFraudCheckPass(event.getTransactionId(),result.getRiskScore(),result.getCheckedBy());

                }



            } catch (RuntimeException e) {
                log.error("Error processing transaction for fraud check: transactionId={}",
                        event.getTransactionId(), e);
                // For now: publish failure event
                fraudEventProducer.publishFraudCheckFailed(event.getTransactionId(),
                        "Fraud check system error: " + e.getMessage(), 100);

            }


    }




}
