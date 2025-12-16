package com.traveler.payment.controller;

import com.traveler.payment.dto.BankTransferRequest;
import com.traveler.payment.dto.PaymentRequest;
import com.traveler.payment.dto.PaymentResponse;
import com.traveler.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/process")
@RequiredArgsConstructor
public class PaymentProcessController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        
        Map<String, Object> result = Map.of(
            "paymentId", response.getPaymentId(),
            "checkoutUrl", "/payment/checkout/" + response.getOrderId() + 
                          "?amount=" + response.getAmount() + 
                          "&currency=" + response.getCurrency() + 
                          "&email=" + response.getCustomerEmail() +
                          "&description=" + (response.getDescription() != null ? response.getDescription() : ""),
            "paymentData", response.getPaymentFormData(),
            "status", "INITIATED"
        );
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/direct")
    public ResponseEntity<PaymentResponse> createDirectPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/bank-transfer")
    public ResponseEntity<Map<String, Object>> initiateBankTransfer(@Valid @RequestBody BankTransferRequest request) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setOrderId(request.getOrderId());
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setCurrency(request.getCurrency());
        paymentRequest.setCustomerEmail(request.getCustomerEmail());
        paymentRequest.setDescription(request.getDescription());
        
        PaymentResponse response = paymentService.createBankTransferPayment(paymentRequest, request.getBankCode());
        
        Map<String, Object> result = Map.of(
            "paymentId", response.getPaymentId(),
            "checkoutUrl", "/payment/checkout/" + response.getOrderId() + 
                          "?amount=" + response.getAmount() + 
                          "&currency=" + response.getCurrency() + 
                          "&email=" + response.getCustomerEmail() +
                          "&bank=" + request.getBankCode() +
                          "&description=" + (response.getDescription() != null ? response.getDescription() : ""),
            "paymentData", response.getPaymentFormData(),
            "status", "INITIATED",
            "paymentMethod", "BANK_TRANSFER"
        );
        
        return ResponseEntity.ok(result);
    }
}