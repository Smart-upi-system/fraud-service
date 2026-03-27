package com.ums.fraud_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "fraud_checks", indexes = {
        @Index(name = "idx_transaction_id", columnList = "transactionId"),
        @Index(name = "idx_sender_id", columnList = "senderId"),
        @Index(name = "idx_fraud_detected", columnList = "fraudDetected"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class FraudCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID transactionId;

    @Column(nullable = false)
    private UUID senderId;

    @Column(nullable = false)
    private UUID receiverId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean fraudDetected;

    @Column(nullable = false)
    private int riskScore; // 0-100

    @Column(length = 1000)
    private String reason; // Why it was flagged/passed

    @Column(length = 500)
    private String triggeredRules; // Comma-separated list of triggered rules

    @Column(length = 50)
    private String checkedBy; // Service instance that checked

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
