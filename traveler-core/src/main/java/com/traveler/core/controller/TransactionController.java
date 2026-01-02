package com.traveler.core.controller;

import com.traveler.common.dto.provider.SaveTrandDTO;
import com.traveler.common.entity.Transaction;
import com.traveler.common.entity.TransactionItem;
import com.traveler.core.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transaction")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/process-payment")
    public ResponseEntity<String> processPayment(@RequestBody Map<String, Object> paymentData) {
        try {
            Double subtotal = Double.valueOf(paymentData.get("subtotal").toString());
            Double taxAmount = Double.valueOf(paymentData.get("taxAmount").toString());
            Double taxRate = Double.valueOf(paymentData.get("taxRate").toString());
            Double totalAmount = Double.valueOf(paymentData.get("totalAmount").toString());
            String customerName = paymentData.get("customerName").toString();
            String customerTenant = paymentData.get("customerTenant").toString();
            List<String> orderCodes = (List<String>) paymentData.get("orderCodes");

            List<Transaction> transaction = transactionService.processPayment(subtotal, taxAmount, taxRate, totalAmount, customerName, customerTenant, orderCodes);
            return ResponseEntity.ok("Payment processed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Payment processing failed: " + e.getMessage());
        }
    }

    @PostMapping("/provider-tran")
    public ResponseEntity<String> processPaymentProvider(@RequestBody SaveTrandDTO saveTrandDTO) {
        try {
            transactionService.processPaymentProvider(saveTrandDTO.getTransaction(),saveTrandDTO.getItem());
            return ResponseEntity.ok("transaction done");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getTransactionHistory() {
        try {
            List<Transaction> transactions = transactionService.getTransactionHistory();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
