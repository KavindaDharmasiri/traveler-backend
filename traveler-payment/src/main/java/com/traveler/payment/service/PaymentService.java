package com.traveler.payment.service;

import com.traveler.payment.dto.PaymentRequest;
import com.traveler.payment.dto.PaymentResponse;
import com.traveler.payment.dto.ProviderBankDetails;
import com.traveler.payment.dto.RefundRequest;
import com.traveler.payment.entity.Payment;
import com.traveler.payment.exception.PaymentException;
import com.traveler.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PayHereService payHereService;
    private final PaymentDistributionService distributionService;
    
    @Value("${payhere.api.checkout-url}")
    private String checkoutUrl;
    
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            // Check for duplicate order payment
            if (paymentRepository.existsByOrderIdAndStatus(request.getOrderId(), Payment.PaymentStatus.COMPLETED)) {
                throw new PaymentException("Payment already completed for this order", "DUPLICATE_PAYMENT");
            }
            
            // Create payment record
            Payment payment = new Payment();
            String paymentId = UUID.randomUUID().toString();
            payment.setPaymentId(paymentId);
            payment.setUserId(request.getUserId());
            payment.setOrderId(request.getOrderId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency().toUpperCase());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentMethod(Payment.PaymentMethod.CARD);
            payment.setDescription(request.getDescription());
            payment.setCustomerEmail(request.getCustomerEmail());
            
            payment = paymentRepository.save(payment);
            
            // Create PayHere payment request
            Map<String, String> paymentData = payHereService.createPaymentRequest(
                request.getOrderId(),
                request.getAmount(),
                request.getCurrency(),
                request.getCustomerEmail(),
                "Customer", // firstName
                "", // lastName
                request.getDescription()
            );
            
            PaymentResponse response = PaymentResponse.fromEntity(payment);
            response.setCheckoutUrl(checkoutUrl);
            
            // Add payment form data for frontend
            response.setPaymentFormData(paymentData);
            
            log.info("Payment created successfully: {}", payment.getPaymentId());
            return response;
            
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment", "PAYMENT_CREATION_ERROR", e);
        }
    }
    
    @Transactional
    public void processPayHereNotification(Map<String, String> paymentData) {
        try {
            if (!payHereService.verifyPayment(paymentData)) {
                throw new PaymentException("Invalid payment verification", "VERIFICATION_FAILED");
            }
            
            String orderId = paymentData.get("order_id");
            String statusCode = paymentData.get("status_code");
            String payHerePaymentId = paymentData.get("payment_id");
            
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            if (payments.isEmpty()) {
                throw new PaymentException("Payment not found for order: " + orderId, "PAYMENT_NOT_FOUND");
            }
            
            Payment payment = payments.get(0);
            payment.setPayHerePaymentId(payHerePaymentId);
            payment.setStatus(mapPayHereStatusToPaymentStatus(statusCode));
            
            if ("2".equals(statusCode)) { // Success
                payment.setCompletedAt(LocalDateTime.now());
                
                // Trigger payment distribution to providers
                triggerPaymentDistribution(payment);
            }
            
            paymentRepository.save(payment);
            log.info("Payment notification processed: {}", orderId);
            
        } catch (Exception e) {
            log.error("Error processing PayHere notification: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process payment notification", "NOTIFICATION_ERROR", e);
        }
    }
    
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentException("Payment not found", "PAYMENT_NOT_FOUND"));
        
        return PaymentResponse.fromEntity(payment);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(String userId) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public PaymentResponse refundPayment(RefundRequest request) {
        Payment payment = paymentRepository.findByPaymentId(request.getPaymentId())
            .orElseThrow(() -> new PaymentException("Payment not found", "PAYMENT_NOT_FOUND"));
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot refund non-completed payment", "INVALID_PAYMENT_STATUS");
        }
        
        // Note: PayHere refunds are typically processed manually
        // This marks the payment as refunded in our system
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        
        payment = paymentRepository.save(payment);
        
        log.info("Payment marked as refunded: {}", request.getPaymentId());
        return PaymentResponse.fromEntity(payment);
    }
    
    @Transactional
    public void updatePaymentStatus(String payHerePaymentId, String statusCode) {
        Payment payment = paymentRepository.findByPayHerePaymentId(payHerePaymentId)
            .orElseThrow(() -> new PaymentException("Payment not found for PayHere PaymentId", "PAYMENT_NOT_FOUND"));
        
        Payment.PaymentStatus newStatus = mapPayHereStatusToPaymentStatus(statusCode);
        payment.setStatus(newStatus);
        
        if (newStatus == Payment.PaymentStatus.COMPLETED) {
            payment.setCompletedAt(LocalDateTime.now());
        }
        
        paymentRepository.save(payment);
        log.info("Payment status updated: {} -> {}", payment.getPaymentId(), newStatus);
    }
    
    @Transactional
    public PaymentResponse createBankTransferPayment(PaymentRequest request, String bankCode) {
        try {
            if (paymentRepository.existsByOrderIdAndStatus(request.getOrderId(), Payment.PaymentStatus.COMPLETED)) {
                throw new PaymentException("Payment already completed for this order", "DUPLICATE_PAYMENT");
            }
            
            Payment payment = new Payment();
            String paymentId = UUID.randomUUID().toString();
            payment.setPaymentId(paymentId);
            payment.setUserId(request.getUserId());
            payment.setOrderId(request.getOrderId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency().toUpperCase());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
            payment.setDescription(request.getDescription());
            payment.setCustomerEmail(request.getCustomerEmail());
            
            payment = paymentRepository.save(payment);
            
            Map<String, String> paymentData = payHereService.createPaymentRequest(
                request.getOrderId(),
                request.getAmount(),
                request.getCurrency(),
                request.getCustomerEmail(),
                "Customer",
                "",
                request.getDescription(),
                bankCode
            );
            
            PaymentResponse response = PaymentResponse.fromEntity(payment);
            response.setCheckoutUrl(checkoutUrl);
            response.setPaymentFormData(paymentData);
            
            log.info("Bank transfer payment created: {}", payment.getPaymentId());
            return response;
            
        } catch (Exception e) {
            log.error("Error creating bank transfer payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create bank transfer payment", "BANK_TRANSFER_ERROR", e);
        }
    }
    
    private Payment.PaymentStatus mapPayHereStatusToPaymentStatus(String statusCode) {
        return switch (statusCode) {
            case "2" -> Payment.PaymentStatus.COMPLETED; // Success
            case "0" -> Payment.PaymentStatus.PENDING; // Pending
            case "-1" -> Payment.PaymentStatus.CANCELLED; // Canceled
            case "-2" -> Payment.PaymentStatus.FAILED; // Failed
            case "-3" -> Payment.PaymentStatus.FAILED; // Chargedback
            default -> Payment.PaymentStatus.FAILED;
        };
    }
    
    private void triggerPaymentDistribution(Payment payment) {
        try {
            // Get provider details from order service
            List<ProviderBankDetails> providers = getProviderDetailsFromOrder(payment.getOrderId());
            
            if (!providers.isEmpty()) {
                distributionService.distributePayment(payment.getPaymentId(), providers);
                log.info("Payment distribution initiated for payment: {}", payment.getPaymentId());
            }
        } catch (Exception e) {
            log.error("Failed to initiate payment distribution: {}", e.getMessage(), e);
        }
    }
    
    private List<ProviderBankDetails> getProviderDetailsFromOrder(String orderId) {
        // This would call your core service to get provider details
        // For now, return empty list - implement based on your order structure
        return List.of();
    }
}