package com.traveler.core.service.feign;

import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.UserResponse;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
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

    @GetMapping("/order/getAllForTraveller")
    ResponseEntity<List<ProviderItemGroupDTO>> getAllForTraveller();

    @GetMapping("/order/getItemForTraveler/{itemId}/{tenant}")
    ResponseEntity<ItemDTO> getItemForTraveler(@PathVariable("itemId") Long itemId,@PathVariable("tenant") String tenant);

    @GetMapping("/auth/user")
    UserResponse getUserByTenant(@RequestParam("tenant") String tenant);
}
