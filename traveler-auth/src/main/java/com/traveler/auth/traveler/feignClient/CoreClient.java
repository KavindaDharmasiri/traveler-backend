package com.traveler.auth.traveler.feignClient;

import com.traveler.common.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "traveler-core", configuration = com.traveler.auth.traveler.config.FeignConfig.class)
public interface CoreClient {

    @PostMapping("/api/v1/order")
    ResponseEntity<String> createOrder(@RequestBody OrderDTO orderDTO, @RequestHeader("X-Tenant-Id") String tenantId);

}
