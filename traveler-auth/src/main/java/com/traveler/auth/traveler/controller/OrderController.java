package com.traveler.auth.traveler.controller;

import com.traveler.auth.traveler.dto.AuthResponse;
import com.traveler.auth.traveler.dto.LoginRequest;
import com.traveler.auth.traveler.dto.RegisterRequest;
import com.traveler.auth.traveler.dto.UserResponse;
import com.traveler.auth.traveler.service.AuthService;
import com.traveler.auth.traveler.service.OrderService;
import com.traveler.auth.traveler.service.UserService;
import com.traveler.common.dto.BulkOrderStatusUpdateDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.SaveOrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import com.traveler.common.entity.Backpack;
import com.traveler.common.entity.Order;
import com.traveler.common.entity.Transaction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
        System.out.println("=== ORDER CONTROLLER INITIALIZED ===");
        System.out.println("Mapping: /order");
    }

    private final OrderService orderService;

    @PostMapping()
    public ResponseEntity<String> createOrder(@RequestBody SaveOrderDTO saveOrderDTO) {
        return orderService.createOrder(saveOrderDTO.getOrder(),saveOrderDTO.getBackpacks());
    }

    @PostMapping("/changeStatus")
    public ResponseEntity<String> changeStatus(@RequestBody Map<String, String> map) {
        String orderId = map.get("orderId");
        String itemId = map.get("itemId");
        String status = map.get("status");
        String tenant = map.get("tenant");
        return orderService.changeStatus(orderId, itemId, status, tenant);
    }

    @PostMapping("/saveTran")
    public ResponseEntity<String> saveTran(@RequestBody List<Transaction> transactions) {
        return orderService.saveTran(transactions);
    }

    @GetMapping()
    public ResponseEntity<List<OrderDTO>> getOrders() {
        return orderService.findAllOrders();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {

        try {
            ResponseEntity<OrderDTO> response = orderService.findOrder(orderId);
            System.out.println("Response status: " + response.getStatusCode());
            return response;
        } catch (Exception e) {
            System.out.println("Controller exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @GetMapping("/getAllForTraveller")
    public ResponseEntity<Map<String, Object>> getAllForTraveller(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "36") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating) {
        return orderService.getAllForTraveller(page, size, category, provider, minPrice, maxPrice, minRating);
    }
    
    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilters() {
        return orderService.getFilters();
    }

    @GetMapping("/getItemForTraveler/{itemId}/{tenant}")
    public ResponseEntity<ItemDTO> getItemForTraveler(@PathVariable Long itemId,
                                                      @PathVariable String tenant) {
        return orderService.getItemForTraveler(itemId,tenant);
    }
}
