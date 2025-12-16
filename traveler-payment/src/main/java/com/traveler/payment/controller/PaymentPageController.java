package com.traveler.payment.controller;

import com.traveler.payment.service.PayHereService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentPageController {
    
    private final PayHereService payHereService;
    
    @Value("${payhere.merchant.id}")
    private String merchantId;
    
    @GetMapping("/checkout/{orderId}")
    public String showPaymentPage(@PathVariable String orderId,
                                @RequestParam String amount,
                                @RequestParam String currency,
                                @RequestParam String email,
                                @RequestParam(required = false) String description,
                                Model model) {
        
        Map<String, String> paymentData = payHereService.createPaymentRequest(
            orderId,
            new BigDecimal(amount),
            currency,
            email,
            "Customer",
            "",
            description != null ? description : "Travel Service Payment"
        );
        
        model.addAttribute("merchantId", merchantId);
        model.addAttribute("paymentData", paymentData);
        
        return "payment-form";
    }
}