package com.traveler.notification.service;

import com.traveler.common.entity.PhoneVerification;
import com.traveler.notification.repository.PhoneVerificationRepository;
import com.traveler.notification.client.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final AuthClient authClient;
    private final TwilioService twilioService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${whatsapp.api.url:https://graph.facebook.com/v18.0}")
    private String whatsappApiUrl;

    @Value("${whatsapp.phone.number.id:YOUR_PHONE_NUMBER_ID}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token:YOUR_ACCESS_TOKEN}")
    private String accessToken;

    @Transactional
    public String sendVerificationCode(String userTenant, String phoneNumber) {
        // Clean up any existing verification for this user/phone
        phoneVerificationRepository.deleteByUserTenantAndPhoneNumber(userTenant, phoneNumber);

        // Generate 6-digit PIN
        String pin = String.format("%06d", new Random().nextInt(1000000));

        // Save verification record
        PhoneVerification verification = new PhoneVerification();
        verification.setUserId(userTenant); // Using tenant as user ID for now
        verification.setUserTenant(userTenant);
        verification.setPhoneNumber(phoneNumber);
        verification.setPin(pin);
        phoneVerificationRepository.save(verification);
        System.out.println(pin);
        // Send WhatsApp message
//        sendWhatsAppMessage(phoneNumber, pin);
        return twilioService.sendWhatsAppMessage(phoneNumber, pin);
    }

    private void sendWhatsAppMessage(String phoneNumber, String pin) {
        try {
            String url = whatsappApiUrl + "/" + phoneNumberId + "/messages";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> message = new HashMap<>();
            message.put("messaging_product", "whatsapp");
            message.put("to", phoneNumber);
            message.put("type", "text");
            
            Map<String, String> text = new HashMap<>();
            text.put("body", "Your Traveler verification code is: " + pin + ". This code will expire in 5 minutes.");
            message.put("text", text);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp message sent successfully to {}", phoneNumber);
            } else {
                log.error("Failed to send WhatsApp message. Status: {}, Response: {}", 
                         response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error sending WhatsApp message to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }

    @Transactional
    public boolean verifyCode(String userTenant, String phoneNumber, String pin) {
        Optional<PhoneVerification> verificationOpt = phoneVerificationRepository
                .findByUserTenantAndPhoneNumberAndIsVerifiedFalseAndExpiresAtAfter(
                        userTenant, phoneNumber, LocalDateTime.now());

        if (verificationOpt.isEmpty()) {
            return false;
        }

        PhoneVerification verification = verificationOpt.get();
        if (verification.getPin().equals(pin)) {
            verification.setIsVerified(true);
            verification.setVerifiedAt(LocalDateTime.now());
            phoneVerificationRepository.save(verification);
            
            // Update phone verification status in auth service
            try {
                authClient.updatePhoneVerificationStatus(userTenant, phoneNumber);
            } catch (Exception e) {
                log.error("Failed to update phone verification status in auth service: {}", e.getMessage());
            }
            
            return true;
        }

        return false;
    }
}
