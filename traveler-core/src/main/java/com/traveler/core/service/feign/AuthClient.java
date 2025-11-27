package com.traveler.core.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "traveler-auth")
public interface AuthClient {
    
    @PostMapping("/auth/validate")
    String validateToken(@RequestHeader("Authorization") String token);
}
