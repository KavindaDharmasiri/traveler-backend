package com.traveler.payment.service;

import com.traveler.payment.config.PayHereConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayHereService {
    
    private final PayHereConfig payHereConfig;
    
    @Value("${payhere.api.checkout-url}")
    private String checkoutUrl;
    
    @Value("${payhere.notify.url}")
    private String notifyUrl;
    
    @Value("${payhere.return.url}")
    private String returnUrl;
    
    @Value("${payhere.cancel.url}")
    private String cancelUrl;
    
    public Map<String, String> createPaymentRequest(String orderId, BigDecimal amount, 
                                                   String currency, String customerEmail, 
                                                   String firstName, String lastName, String description) {
        return createPaymentRequest(orderId, amount, currency, customerEmail, firstName, lastName, description, null);
    }
    
    public Map<String, String> createPaymentRequest(String orderId, BigDecimal amount, 
                                                   String currency, String customerEmail, 
                                                   String firstName, String lastName, String description, String bankCode) {
        
        Map<String, String> paymentData = new HashMap<>();
        
        // Required PayHere parameters
        paymentData.put("merchant_id", payHereConfig.getMerchantId());
        paymentData.put("return_url", returnUrl);
        paymentData.put("cancel_url", cancelUrl);
        paymentData.put("notify_url", notifyUrl);
        paymentData.put("order_id", orderId);
        paymentData.put("items", description != null ? description : "Travel Service Payment");
        paymentData.put("currency", currency);
        paymentData.put("amount", amount.toString());
        
        // Customer details
        paymentData.put("first_name", firstName != null ? firstName : "Customer");
        paymentData.put("last_name", lastName != null ? lastName : "");
        paymentData.put("email", customerEmail);
        paymentData.put("phone", "");
        paymentData.put("address", "");
        paymentData.put("city", "");
        paymentData.put("country", "Sri Lanka");
        
        // Bank transfer specific
        if (bankCode != null && !bankCode.isEmpty()) {
            paymentData.put("delivery_address", "");
            paymentData.put("delivery_city", "");
            paymentData.put("delivery_country", "Sri Lanka");
            paymentData.put("custom_1", bankCode);
            paymentData.put("custom_2", "BANK_TRANSFER");
        }
        
        // Generate hash
        String hash = generateHash(paymentData);
        paymentData.put("hash", hash);
        
        return paymentData;
    }
    
    private String generateHash(Map<String, String> data) {
        try {
            String hashString = payHereConfig.getMerchantId() + 
                              data.get("order_id") + 
                              data.get("amount") + 
                              data.get("currency") + 
                              getMD5Hash(payHereConfig.getMerchantSecret());
            
            return getMD5Hash(hashString).toUpperCase();
        } catch (Exception e) {
            log.error("Error generating PayHere hash: {}", e.getMessage());
            throw new RuntimeException("Failed to generate payment hash", e);
        }
    }
    
    public boolean verifyPayment(Map<String, String> paymentData) {
        try {
            String receivedHash = paymentData.get("md5sig");
            String orderId = paymentData.get("order_id");
            String paymentId = paymentData.get("payment_id");
            String amount = paymentData.get("payhere_amount");
            String currency = paymentData.get("payhere_currency");
            String statusCode = paymentData.get("status_code");
            
            String hashString = payHereConfig.getMerchantId() + 
                              orderId + 
                              amount + 
                              currency + 
                              statusCode + 
                              getMD5Hash(payHereConfig.getMerchantSecret());
            
            String calculatedHash = getMD5Hash(hashString).toUpperCase();
            
            return calculatedHash.equals(receivedHash);
        } catch (Exception e) {
            log.error("Error verifying PayHere payment: {}", e.getMessage());
            return false;
        }
    }
    
    private String getMD5Hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}