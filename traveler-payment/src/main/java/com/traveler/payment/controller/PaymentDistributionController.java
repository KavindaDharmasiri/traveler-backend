package com.traveler.payment.controller;

import com.traveler.payment.dto.ProviderBankDetails;
import com.traveler.payment.entity.PaymentDistribution;
import com.traveler.payment.service.PaymentDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/distribution")
@RequiredArgsConstructor
public class PaymentDistributionController {
    
    private final PaymentDistributionService distributionService;
    
    @PostMapping("/{paymentId}")
    public ResponseEntity<String> distributePayment(
            @PathVariable String paymentId,
            @RequestBody List<ProviderBankDetails> providers) {
        
        distributionService.distributePayment(paymentId, providers);
        return ResponseEntity.ok("Payment distribution initiated");
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<List<PaymentDistribution>> getPaymentDistributions(@PathVariable String paymentId) {
        List<PaymentDistribution> distributions = distributionService.getPaymentDistributions(paymentId);
        return ResponseEntity.ok(distributions);
    }
    
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<PaymentDistribution>> getProviderDistributions(@PathVariable String providerId) {
        List<PaymentDistribution> distributions = distributionService.getProviderDistributions(providerId);
        return ResponseEntity.ok(distributions);
    }
    
    @PostMapping("/process/{paymentId}")
    public ResponseEntity<String> processDistributions(@PathVariable String paymentId) {
        distributionService.processDistributions(paymentId);
        return ResponseEntity.ok("Distribution processing initiated");
    }
}