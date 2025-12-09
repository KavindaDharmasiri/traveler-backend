package com.traveler.core.service.feign;

import com.traveler.common.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "traveler-notification")
public interface NotificationClient {
    
    @PostMapping("/notifications")
    Map<String, Object> sendNotification(@RequestBody NotificationDTO request);
}
