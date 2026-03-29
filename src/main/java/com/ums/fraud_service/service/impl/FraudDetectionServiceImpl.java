package com.ums.fraud_service.service.impl;



import com.ums.fraud_service.events.TransactionInitiatedEvent;
import com.ums.fraud_service.model.FraudCheck;
import com.ums.fraud_service.model.dto.FraudCheckResult;
import com.ums.fraud_service.repository.FraudCheckRepository;
import com.ums.fraud_service.rules.FraudRules;
import com.ums.fraud_service.rules.RuleResult;
import com.ums.fraud_service.service.FraudDetectionService;
import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudCheckRepository fraudCheckRepository;
    private final List<FraudRules> fraudRules;
    private final ModelMapper modelMapper;

    @Value("${fraud.rules.fraud-threshold:70}")
    private int fraudThreshold;

    @Override
    @Transactional
    public FraudCheckResult analyzeTransaction(TransactionInitiatedEvent event) {
        log.info("Analyzing transaction for fraud: transactionId={}, senderId={}, amount={}",
                event.getTransactionId(), event.getSenderId(), event.getAmount());

        List<String> failedRules=new ArrayList<>();

//        FraudCheck fraudCheckExist= fraudCheckRepository.findByTransactionId(event.getTransactionId()).orElseThrow(()->new DuplicateTransactionException("Already exist transaction"));
        Optional<FraudCheck> fraudCheckExist = fraudCheckRepository
                .findByTransactionId(event.getTransactionId());
        if  (fraudCheckExist.isPresent()){
            log.warn("Transaction already analyzed: transactionId={}", event.getTransactionId());
            return modelMapper.map(fraudCheckExist.get(),FraudCheckResult.class);
        }

        List<RuleResult> triggeredRules = new ArrayList<>();
        int totalRiskScore = 0;

        List<FraudRules> sortedRules=fraudRules.stream().sorted(Comparator.comparingInt(FraudRules::getPriority))
                .collect(Collectors.toList());

        for(FraudRules rule : sortedRules){
            try{
                RuleResult result = rule.evaluate(event);
                if (result.isTriggered()) {
                    log.info("Fraud rule triggered: rule={}, score={}, reason={}",
                            rule.getRuleName(), result.getScoreContribution(), result.getReason());
                    triggeredRules.add(result);
                    totalRiskScore += result.getScoreContribution();
                }

            }catch (Exception e){
                log.error("Error evaluating fraud rule: rule={}, transactionId={}",
                        rule.getRuleName(), event.getTransactionId(), e);
                failedRules.add(rule.getRuleName());

                if("VELOCITY".equals(rule.getRuleName())){
                    log.warn("VelocityRule failed → adding safety penalty");
                    totalRiskScore += 30;
                }


            }
        }

        totalRiskScore = Math.min(totalRiskScore, 100);
        boolean fraudDetected = totalRiskScore >= fraudThreshold;


            String reason;

            if (!failedRules.isEmpty()) {
                reason = String.format(
                        "Partial fraud check. Failed rules: %s. Triggers: %s",
                        String.join(",", failedRules),
                        triggeredRules.stream()
                                .map(RuleResult::getReason)
                                .collect(Collectors.joining("; "))
                );
            } else if (triggeredRules.isEmpty()) {
                reason = "No fraud indicators detected";
            } else {
                reason = triggeredRules.stream()
                        .map(RuleResult::getReason)
                        .collect(Collectors.joining("; "));
            }

            String triggeredRuleNames = sortedRules.stream()
                    .filter(rl -> triggeredRules.stream()
                            .anyMatch(r -> r.getReason().contains(rl.getRuleName())))
                    .map(FraudRules::getRuleName)
                    .collect(Collectors.joining(","));

            FraudCheck fraudCheck=FraudCheck.builder()
                    .transactionId(event.getTransactionId())
                    .senderId(event.getSenderId())
                    .receiverId(event.getReceiverId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .fraudDetected(fraudDetected)
                    .riskScore(totalRiskScore)
                    .reason(reason)
                    .triggeredRules(triggeredRuleNames)
                    .checkedBy(getServiceInstance())
                    .build();

            fraudCheckRepository.save(fraudCheck);

        log.info("Fraud analysis complete: transactionId={}, fraudDetected={}, riskScore={}",
                event.getTransactionId(), fraudDetected, totalRiskScore);

            return modelMapper.map(fraudCheck,FraudCheckResult.class);
    }

//    private String buildReason(List<RuleResult> triggeredRules, boolean fraudDetected, int totalRiskScore) {
//
//    }

    /**
     * Get service instance identifier
     */
    private String getServiceInstance() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "fraud-service";
        }
    }
}
