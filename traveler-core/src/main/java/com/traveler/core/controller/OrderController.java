package com.traveler.core.controller;

import com.traveler.common.dto.*;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.entity.Backpack;
import com.traveler.common.entity.Order;
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
    public ResponseEntity<String> createOrder(@RequestBody SeparateSaveOrderDTO saveOrderDTO) {
        return orderService.createOrder(saveOrderDTO.getOrder(), saveOrderDTO.getBackpacks());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrderWithSup(@RequestBody List<OrderDTO> orderDTOs) {
        return orderService.createOrderWithSup(orderDTOs);
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

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<OrderWithItemsDTO> getOrderByCode(@PathVariable String orderCode) {
        return orderService.findOrderByCode(orderCode);
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateStatusWithSup(@RequestBody BulkOrderStatusUpdateDTO updateDTO) {
        return orderService.updateStatusWithSup(updateDTO);
    }

    @PutMapping("/bulk-update-status/{orderCode}/{status}")
    public ResponseEntity<String> bulkUpdateOrderStatus(@PathVariable String orderCode, @PathVariable String status, @RequestHeader(required = false) String clientTenant) {
        return orderService.bulkUpdateOrderStatus(orderCode, status);
    }

    @PutMapping("/updateStatusSingle/{orderId}/{itemId}/{status}")
    public ResponseEntity<String> updateStatusSingle(@PathVariable Long orderId, @PathVariable String itemId, @PathVariable String status) {
        Long itemIdLong = "null".equals(itemId) ? null : Long.parseLong(itemId);
        return orderService.updateStatusSingle(orderId, itemIdLong, status);
    }

    @PostMapping("/auth-update-status")
    public ResponseEntity<String> authUpdatStatus(@RequestBody AuthUpdateStatusDTO map) {
        String orderId = map.getOrderId();
        String itemId = map.getItemId();
        String status = map.getStatus();
        return orderService.authUpdatStatus(orderId, itemId, status);
    }

}
