package com.traveler.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_distributions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDistribution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String paymentId;
    
    @Column(nullable = false)
    private String providerId;
    
    @Column(nullable = false)
    private String providerBankAccount;
    
    @Column(nullable = false)
    private String providerBankCode;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistributionStatus status;
    
    private String transferReference;
    private String failureReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum DistributionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
}