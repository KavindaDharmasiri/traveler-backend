package com.traveler.payment.controller;

import com.traveler.payment.dto.PaymentRequest;
import com.traveler.payment.dto.PaymentResponse;
import com.traveler.payment.dto.RefundRequest;
import com.traveler.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Creating payment for user: {}, order: {}", request.getUserId(), request.getOrderId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }
    

    
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable String userId) {
        List<PaymentResponse> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@Valid @RequestBody RefundRequest request) {
        log.info("Processing refund for payment: {}", request.getPaymentId());
        PaymentResponse response = paymentService.refundPayment(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "traveler-payment",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}