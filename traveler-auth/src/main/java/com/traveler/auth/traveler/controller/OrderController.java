package com.traveler.auth.traveler.controller;

import com.traveler.auth.traveler.dto.AuthResponse;
import com.traveler.auth.traveler.dto.LoginRequest;
import com.traveler.auth.traveler.dto.RegisterRequest;
import com.traveler.auth.traveler.dto.UserResponse;
import com.traveler.auth.traveler.service.AuthService;
import com.traveler.auth.traveler.service.OrderService;
import com.traveler.auth.traveler.service.UserService;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<String> createOrder( @RequestBody OrderDTO orderDTO) {
        return orderService.createOrder(orderDTO);
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
    public ResponseEntity<List<ProviderItemGroupDTO>> getAllForTraveller() {
        return orderService.getAllForTraveller();
    }

    @GetMapping("/getItemForTraveler/{itemId}/{tenant}")
    public ResponseEntity<ItemDTO> getItemForTraveler(@PathVariable Long itemId,
                                                      @PathVariable String tenant) {
        System.out.println("awaaaaaa");
        return orderService.getItemForTraveler(itemId,tenant);
    }
}
