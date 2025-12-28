package com.traveler.core.service.feign;

import com.traveler.common.dto.BulkOrderStatusUpdateDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.SaveOrderDTO;
import com.traveler.common.dto.UserResponse;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import com.traveler.common.entity.Backpack;
import com.traveler.common.entity.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "traveler-auth", configuration = com.traveler.core.config.FeignConfig.class)
public interface AuthClient {
    
    @PostMapping("/auth/validate")
    String validateToken(@RequestHeader("Authorization") String token);

    @PostMapping("/order")
    String saveOrder(@RequestBody SaveOrderDTO saveOrderDTO);

    @GetMapping("/order")
    ResponseEntity<List<OrderDTO>> getOrders();

    @GetMapping("/order/{orderId}")
    ResponseEntity<OrderDTO> getOrder(@PathVariable("orderId") Long orderId);

    @GetMapping("/order/getAllForTraveller")
    ResponseEntity<Map<String, Object>> getAllForTraveller(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minRating", required = false) Double minRating);
    
    @GetMapping("/order/filters")
    ResponseEntity<Map<String, Object>> getFilters();

    @GetMapping("/order/getItemForTraveler/{itemId}/{tenant}")
    ResponseEntity<ItemDTO> getItemForTraveler(@PathVariable("itemId") Long itemId,@PathVariable("tenant") String tenant);

    @GetMapping("/auth/user")
    UserResponse getUserByTenant(@RequestParam("tenant") String tenant);

    @GetMapping("/auth/users")
    ResponseEntity<List<UserResponse>> getAllUsers();

    @GetMapping("/auth/users/type/{type}")
    ResponseEntity<List<UserResponse>> getUsersByType(@PathVariable("type") String type);

    @PostMapping("/order/changeStatus")
    String updateOrderStatus(@RequestBody Map map);

}
