package com.traveler.notification.controller;

import com.traveler.common.dto.PhoneVerificationRequestDTO;
import com.traveler.common.dto.PhoneVerificationConfirmDTO;
import com.traveler.notification.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationCode(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody PhoneVerificationRequestDTO request) {
        try {
            whatsAppService.sendVerificationCode(tenantId, request.getPhoneNumber());
            return ResponseEntity.ok("Verification code sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send verification code: " + e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody PhoneVerificationConfirmDTO request) {
        try {
            boolean isValid = whatsAppService.verifyCode(tenantId, request.getPhoneNumber(), request.getPin());
            if (isValid) {
                return ResponseEntity.ok("Phone number verified successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired verification code");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }
}