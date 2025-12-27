package com.traveler.auth.traveler.feignClient;

import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.SeparateSaveOrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.entity.Backpack;
import com.traveler.common.entity.Order;
import com.traveler.common.utils.STATUS;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "traveler-core", configuration = com.traveler.auth.traveler.config.FeignConfig.class)
public interface CoreClient {

    @PostMapping("/api/v1/order")
    ResponseEntity<String> createOrder(@RequestBody SeparateSaveOrderDTO saveOrderDTO, @RequestHeader("X-Tenant-Id") String tenantId);

    @GetMapping("/api/v1/provider/item/getItemsForTraveller")
    List<ItemDetailsDTO> getItemsForTraveller(@RequestHeader("X-Tenant-Id") String tenantId);

    @GetMapping("/api/v1/provider/item/{itemId}")
    ResponseEntity<ItemDTO> getItem(@PathVariable Long itemId, @RequestHeader("X-Tenant-Id") String tenantId);

    @PutMapping("/api/v1/order/bulk-update-status/{orderCode}/{status}")
    String updateStatus(@PathVariable String orderCode, @PathVariable String status, @RequestHeader("X-Tenant-Id") String clientTenant);

    @PostMapping("/api/v1/order/auth-update-status")
    ResponseEntity<String> updateOrderStatus(@RequestBody Map map, @RequestHeader("X-Tenant-Id") String tenant);
}
