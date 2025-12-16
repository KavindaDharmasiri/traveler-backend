package com.traveler.payment.service;

import com.traveler.payment.dto.ProviderBankDetails;
import com.traveler.payment.entity.Payment;
import com.traveler.payment.entity.PaymentDistribution;
import com.traveler.payment.repository.PaymentDistributionRepository;
import com.traveler.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentDistributionService {
    
    private final PaymentDistributionRepository distributionRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    
    @Value("${app.commission.platform-rate:0.05}")
    private BigDecimal platformCommissionRate;
    
    @Transactional
    public void distributePayment(String paymentId, List<ProviderBankDetails> providers) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Cannot distribute incomplete payment");
        }
        
        // Create distribution records
        for (ProviderBankDetails provider : providers) {
            PaymentDistribution distribution = new PaymentDistribution();
            distribution.setPaymentId(paymentId);
            distribution.setProviderId(provider.getProviderId());
            distribution.setProviderBankAccount(provider.getAccountNumber());
            distribution.setProviderBankCode(provider.getBankCode());
            distribution.setAmount(provider.getAmount());
            distribution.setCommissionRate(provider.getCommissionRate());
            
            // Calculate net amount after commission
            BigDecimal commission = provider.getAmount().multiply(provider.getCommissionRate());
            BigDecimal netAmount = provider.getAmount().subtract(commission);
            distribution.setNetAmount(netAmount);
            distribution.setStatus(PaymentDistribution.DistributionStatus.PENDING);
            
            distributionRepository.save(distribution);
            log.info("Created distribution for provider {} - Amount: {}, Net: {}", 
                provider.getProviderId(), provider.getAmount(), netAmount);
        }
        
        // Process distributions
        processDistributions(paymentId);
    }
    
    @Transactional
    public void processDistributions(String paymentId) {
        List<PaymentDistribution> distributions = distributionRepository
            .findByPaymentIdAndStatus(paymentId, PaymentDistribution.DistributionStatus.PENDING);
        
        for (PaymentDistribution distribution : distributions) {
            try {
                distribution.setStatus(PaymentDistribution.DistributionStatus.PROCESSING);
                distribution.setProcessedAt(LocalDateTime.now());
                distributionRepository.save(distribution);
                
                // Simulate bank transfer (replace with actual bank API)
                boolean transferSuccess = initiateProviderTransfer(distribution);
                
                if (transferSuccess) {
                    distribution.setStatus(PaymentDistribution.DistributionStatus.COMPLETED);
                    distribution.setCompletedAt(LocalDateTime.now());
                    distribution.setTransferReference("TXN_" + System.currentTimeMillis());
                    log.info("Distribution completed for provider: {}", distribution.getProviderId());
                } else {
                    distribution.setStatus(PaymentDistribution.DistributionStatus.FAILED);
                    distribution.setFailureReason("Bank transfer failed");
                    log.error("Distribution failed for provider: {}", distribution.getProviderId());
                }
                
                distributionRepository.save(distribution);
                
            } catch (Exception e) {
                distribution.setStatus(PaymentDistribution.DistributionStatus.FAILED);
                distribution.setFailureReason(e.getMessage());
                distributionRepository.save(distribution);
                log.error("Error processing distribution for provider {}: {}", 
                    distribution.getProviderId(), e.getMessage());
            }
        }
    }
    
    private boolean initiateProviderTransfer(PaymentDistribution distribution) {
        try {
            // This would integrate with your bank's API for automated transfers
            // For now, we'll simulate the transfer
            
            Map<String, Object> transferRequest = new HashMap<>();
            transferRequest.put("fromAccount", "YOUR_BUSINESS_ACCOUNT");
            transferRequest.put("toAccount", distribution.getProviderBankAccount());
            transferRequest.put("toBankCode", distribution.getProviderBankCode());
            transferRequest.put("amount", distribution.getNetAmount());
            transferRequest.put("currency", "LKR");
            transferRequest.put("reference", "Provider payout - " + distribution.getPaymentId());
            
            log.info("Initiating transfer: LKR {} to provider {} account {}", 
                distribution.getNetAmount(), 
                distribution.getProviderId(), 
                distribution.getProviderBankAccount());
            
            // Simulate successful transfer (replace with actual bank API call)
            return true;
            
        } catch (Exception e) {
            log.error("Bank transfer failed: {}", e.getMessage());
            return false;
        }
    }
    
    public List<PaymentDistribution> getPaymentDistributions(String paymentId) {
        return distributionRepository.findByPaymentId(paymentId);
    }
    
    public List<PaymentDistribution> getProviderDistributions(String providerId) {
        return distributionRepository.findByProviderId(providerId);
    }
}