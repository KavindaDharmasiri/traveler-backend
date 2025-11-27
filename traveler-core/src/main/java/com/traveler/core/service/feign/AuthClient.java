package com.traveler.core.service.feign;

import com.traveler.common.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "traveler-auth", configuration = com.traveler.core.config.FeignConfig.class)
public interface AuthClient {
    
    @PostMapping("/auth/validate")
    String validateToken(@RequestHeader("Authorization") String token);

    @PostMapping("/order")
    String saveOrder(@RequestBody OrderDTO orderDTO);

    @GetMapping("/order")
    ResponseEntity<List<OrderDTO>> getOrders();

    @GetMapping("/order/{orderId}")
    ResponseEntity<OrderDTO> getOrder(@PathVariable("orderId") Long orderId);
}
