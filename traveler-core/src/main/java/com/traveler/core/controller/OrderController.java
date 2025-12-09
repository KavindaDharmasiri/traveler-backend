package com.traveler.core.controller;

import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.core.service.OrderService;
import com.traveler.core.service.ProviderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping()
    public ResponseEntity<String> createOrder(@RequestBody OrderDTO orderDTO) {
        return orderService.createOrder(orderDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrderWithSup(@RequestBody OrderDTO orderDTO) {
        return orderService.createOrderWithSup(orderDTO);
    }

    @GetMapping()
    public ResponseEntity<Map<String, Map<String, List<OrderDTO>>>> getOrders() {
        return orderService.findAllOrders();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        return orderService.findOrder(orderId);
    }

    @GetMapping("getAdmin")
    public ResponseEntity<List<OrderDTO>> getOrdersFromAdmin() {
        return orderService.findAllOrdersFromAdmin();
    }

    @GetMapping("/getAdmin/{orderId}")
    public ResponseEntity<OrderDTO> getOrderFromAdmin(@PathVariable Long orderId) {
        return orderService.getOrderFromAdmin(orderId);
    }


}
