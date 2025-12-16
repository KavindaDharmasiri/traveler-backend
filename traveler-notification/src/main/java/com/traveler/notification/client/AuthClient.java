package com.traveler.notification.client;

import com.traveler.notification.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "traveler-auth", configuration = FeignConfig.class)
public interface AuthClient {
    
    @PutMapping("/api/v1/phone-verification/update-status/{tenantId}")
    ResponseEntity<String> updatePhoneVerificationStatus(
            @PathVariable String tenantId,
            @RequestParam String phoneNumber);
    
    @PutMapping("/api/v1/email-verification/update-status/{tenantId}")
    ResponseEntity<String> updateEmailVerificationStatus(
            @PathVariable String tenantId,
            @RequestParam String email);
}