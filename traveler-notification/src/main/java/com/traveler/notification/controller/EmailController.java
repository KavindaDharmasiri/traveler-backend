package com.traveler.notification.controller;

import com.traveler.common.dto.EmailVerificationRequestDTO;
import com.traveler.common.dto.EmailVerificationConfirmDTO;
import com.traveler.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationCode(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody EmailVerificationRequestDTO request) {
        try {
            emailService.sendVerificationCode(tenantId, request.getEmail());
            return ResponseEntity.ok("Verification code sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send verification code: " + e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody EmailVerificationConfirmDTO request) {
        try {
            boolean isValid = emailService.verifyCode(tenantId, request.getEmail(), request.getVerificationCode());
            if (isValid) {
                return ResponseEntity.ok("Email verified successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired verification code");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }
}