package com.ums.fraud_service.rules;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class RuleResult {
    private boolean triggered; // True if rule was violated
    private int scoreContribution; // How much to add to risk score (0-100)
    private String reason; // Why rule was triggered
}
