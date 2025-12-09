package com.traveler.auth.traveler.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "traveler-notification")
public interface NotificationClient {
    
    @PostMapping("/notifications")
    Map<String, Object> sendNotification(@RequestBody Map<String, String> request);
}
