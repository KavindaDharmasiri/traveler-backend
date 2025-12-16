package com.traveler.auth.traveler.controller;

import com.traveler.auth.traveler.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/phone-verification")
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationController {

    private final UserService userService;

    @PutMapping("/update-status/{tenantId}")
    public ResponseEntity<String> updatePhoneVerificationStatus(
            @PathVariable String tenantId,
            @RequestParam String phoneNumber) {
        try {
            userService.updatePhoneVerificationStatus(tenantId, phoneNumber);
            log.info("Updating phone verification status for tenant: {} and phone: {}", tenantId, phoneNumber);
            return ResponseEntity.ok("Phone verification status updated successfully");
        } catch (Exception e) {
            log.error("Failed to update phone verification status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update phone verification status");
        }
    }
}
