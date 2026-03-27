package com.ums.fraud_service.repository;

import com.ums.fraud_service.model.FraudCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudCheckRepository extends JpaRepository<FraudCheck,UUID> {
    Optional<FraudCheck> findByTransactionId(UUID transactionId);

    boolean existsByTransactionId(UUID transactionId);

    // Get fraud checks for a sender
    List<FraudCheck> findBySenderIdOrderByCreatedAtDesc(UUID senderId);

    // Count recent fraud attempts by sender
    @Query("SELECT COUNT(f) FROM FraudCheck f WHERE f.senderId = :senderId " +
            "AND f.fraudDetected = true AND f.createdAt >= :since")
    long countFraudAttemptsBySenderSince(UUID senderId, LocalDateTime since);

    // Count transactions by sender in time window (velocity check)
    @Query("SELECT COUNT(f) FROM FraudCheck f WHERE f.senderId = :senderId " +
            "AND f.createdAt >= :since")
    long countTransactionsBySenderSince(UUID senderId, LocalDateTime since);

    // Get statistics
    @Query("SELECT COUNT(f) FROM FraudCheck f WHERE f.fraudDetected = true")
    long countTotalFraudDetected();

    @Query("SELECT AVG(f.riskScore) FROM FraudCheck f WHERE f.createdAt >= :since")
    Double getAverageRiskScoreSince(LocalDateTime since);

}
