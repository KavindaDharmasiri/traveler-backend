package com.traveler.payment.controller;

import com.traveler.payment.dto.PaymentRequest;
import com.traveler.payment.dto.PaymentResponse;
import com.traveler.payment.dto.ProviderBankDetails;
import com.traveler.payment.entity.Payment;
import com.traveler.payment.entity.PaymentDistribution;
import com.traveler.payment.repository.PaymentRepository;
import com.traveler.payment.service.PaymentDistributionService;
import com.traveler.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final PaymentService paymentService;
    private final PaymentDistributionService distributionService;
    private final PaymentRepository paymentRepository;
    
    @PostMapping("/create-mock-payment")
    public ResponseEntity<PaymentResponse> createMockPayment() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("TEST_USER_001");
        request.setOrderId("ORDER_" + System.currentTimeMillis());
        request.setAmount(new BigDecimal("5000.00"));
        request.setCurrency("LKR");
        request.setCustomerEmail("test@example.com");
        request.setDescription("Test travel booking payment");
        
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/simulate-payment-success/{paymentId}")
    public ResponseEntity<String> simulatePaymentSuccess(@PathVariable String paymentId) {
        // Simulate PayHere webhook notification
        Map<String, String> mockWebhookData = new HashMap<>();
        mockWebhookData.put("order_id", getOrderIdByPaymentId(paymentId));
        mockWebhookData.put("payment_id", "PH_" + System.currentTimeMillis());
        mockWebhookData.put("status_code", "2"); // Success
        mockWebhookData.put("payhere_amount", "5000.00");
        mockWebhookData.put("payhere_currency", "LKR");
        mockWebhookData.put("md5sig", "mock_hash_signature");
        
        try {
            // Skip verification for testing
            simulatePaymentCompletion(paymentId);
            return ResponseEntity.ok("Payment marked as successful and distribution initiated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/create-mock-providers/{paymentId}")
    public ResponseEntity<String> createMockProviders(@PathVariable String paymentId) {
        List<ProviderBankDetails> mockProviders = List.of(
            createMockProvider("HOTEL_001", "Grand Hotel Colombo", "7278", "1234567890", new BigDecimal("2000.00")),
            createMockProvider("TRANSPORT_001", "ABC Taxi Service", "7056", "9876543210", new BigDecimal("1500.00")),
            createMockProvider("GUIDE_001", "Tourism Guide Lanka", "7083", "5555666677", new BigDecimal("1000.00"))
        );
        
        distributionService.distributePayment(paymentId, mockProviders);
        return ResponseEntity.ok("Mock providers created and distribution initiated");
    }
    
    @GetMapping("/payment-status/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        PaymentResponse payment = paymentService.getPayment(paymentId);
        List<PaymentDistribution> distributions = distributionService.getPaymentDistributions(paymentId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("payment", payment);
        status.put("distributions", distributions);
        status.put("totalDistributed", distributions.stream()
            .map(PaymentDistribution::getNetAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/complete-test-flow")
    public ResponseEntity<Map<String, Object>> completeTestFlow() {
        try {
            // 1. Create payment
            PaymentResponse payment = createMockPayment().getBody();
            String paymentId = payment.getPaymentId();
            
            // 2. Simulate payment success
            simulatePaymentSuccess(paymentId);
            
            // 3. Create mock providers and distribute
            createMockProviders(paymentId);
            
            // 4. Get final status
            Map<String, Object> result = getPaymentStatus(paymentId).getBody();
            result.put("testCompleted", true);
            result.put("message", "Complete test flow executed successfully");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private ProviderBankDetails createMockProvider(String id, String name, String bankCode, String account, BigDecimal amount) {
        ProviderBankDetails provider = new ProviderBankDetails();
        provider.setProviderId(id);
        provider.setProviderName(name);
        provider.setBankName(getBankName(bankCode));
        provider.setBankCode(bankCode);
        provider.setAccountNumber(account);
        provider.setAccountHolderName(name + " (Pvt) Ltd");
        provider.setBranchCode("001");
        provider.setAmount(amount);
        provider.setCommissionRate(new BigDecimal("0.05")); // 5%
        return provider;
    }
    
    private String getBankName(String bankCode) {
        return switch (bankCode) {
            case "7278" -> "Commercial Bank";
            case "7056" -> "Peoples Bank";
            case "7083" -> "Bank of Ceylon";
            default -> "Unknown Bank";
        };
    }
    
    private String getOrderIdByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
            .map(Payment::getOrderId)
            .orElse("UNKNOWN_ORDER");
    }
    
    private void simulatePaymentCompletion(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPayHerePaymentId("PH_" + System.currentTimeMillis());
        payment.setCompletedAt(java.time.LocalDateTime.now());
        
        paymentRepository.save(payment);
        
        // Trigger distribution
        List<ProviderBankDetails> mockProviders = List.of(
            createMockProvider("HOTEL_001", "Grand Hotel Colombo", "7278", "1234567890", new BigDecimal("2000.00")),
            createMockProvider("TRANSPORT_001", "ABC Taxi Service", "7056", "9876543210", new BigDecimal("1500.00")),
            createMockProvider("GUIDE_001", "Tourism Guide Lanka", "7083", "5555666677", new BigDecimal("1000.00"))
        );
        
        distributionService.distributePayment(paymentId, mockProviders);
    }
}