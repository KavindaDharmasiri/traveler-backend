package com.traveler.payment.controller;

import com.traveler.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/payhere")
    public ResponseEntity<String> handlePayHereNotification(@RequestParam Map<String, String> paymentData) {
        try {
            log.info("Received PayHere notification for order: {}", paymentData.get("order_id"));
            
            paymentService.processPayHereNotification(paymentData);
            
            return ResponseEntity.ok("OK");
            
        } catch (Exception e) {
            log.error("Error processing PayHere notification: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("ERROR");
        }
    }
}