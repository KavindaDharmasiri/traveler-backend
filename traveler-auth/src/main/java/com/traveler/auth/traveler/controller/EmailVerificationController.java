package com.traveler.auth.traveler.controller;

import com.traveler.auth.traveler.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email-verification")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final UserService userService;

    @PutMapping("/update-status/{tenantId}")
    public ResponseEntity<String> updateEmailVerificationStatus(
            @PathVariable String tenantId,
            @RequestParam String email) {
        try {
            userService.updateEmailVerificationStatus(tenantId, email);
            log.info("Updating email verification status for tenant: {} and email: {}", tenantId, email);
            return ResponseEntity.ok("Email verification status updated successfully");
        } catch (Exception e) {
            log.error("Failed to update email verification status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update email verification status");
        }
    }
}