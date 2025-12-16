package com.traveler.payment.repository;

import com.traveler.payment.entity.PaymentDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentDistributionRepository extends JpaRepository<PaymentDistribution, Long> {
    
    List<PaymentDistribution> findByPaymentId(String paymentId);
    
    List<PaymentDistribution> findByProviderId(String providerId);
    
    List<PaymentDistribution> findByStatus(PaymentDistribution.DistributionStatus status);
    
    List<PaymentDistribution> findByPaymentIdAndStatus(String paymentId, PaymentDistribution.DistributionStatus status);
}