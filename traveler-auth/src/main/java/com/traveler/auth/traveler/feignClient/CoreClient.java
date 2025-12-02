package com.traveler.auth.traveler.feignClient;

import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "traveler-core", configuration = com.traveler.auth.traveler.config.FeignConfig.class)
public interface CoreClient {

    @PostMapping("/api/v1/order")
    ResponseEntity<String> createOrder(@RequestBody OrderDTO orderDTO, @RequestHeader("X-Tenant-Id") String tenantId);

    @GetMapping("/api/v1/provider/item/getItemsForTraveller")
    List<ItemDetailsDTO> getItemsForTraveller(@RequestHeader("X-Tenant-Id") String tenantId);

    @GetMapping("/api/v1/provider/item/{itemId}")
    ResponseEntity<ItemDTO> getItem(@PathVariable Long itemId, @RequestHeader("X-Tenant-Id") String tenantId);
}
