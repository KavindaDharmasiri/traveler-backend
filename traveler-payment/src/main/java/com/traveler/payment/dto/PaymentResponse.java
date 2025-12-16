package com.traveler.payment.dto;

import com.traveler.payment.entity.Payment;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PaymentResponse {
    
    private String paymentId;
    private String userId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentStatus status;
    private Payment.PaymentMethod paymentMethod;
    private String payHerePaymentId;
    private String checkoutUrl;
    private Map<String, String> paymentFormData;
    private String clientSecret;
    private String description;
    private String customerEmail;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setUserId(payment.getUserId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPayHerePaymentId(payment.getPayHerePaymentId());
        response.setDescription(payment.getDescription());
        response.setCustomerEmail(payment.getCustomerEmail());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        return response;
    }
}