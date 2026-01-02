package com.traveler.core.service;

import com.traveler.common.dto.BulkOrderStatusUpdateDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.OrderWithItemsDTO;
import com.traveler.common.entity.Backpack;
import com.traveler.common.entity.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

public interface OrderService {
    ResponseEntity<String> createOrder(Order orderDTO, Backpack backPack);

    ResponseEntity<String> createOrderWithSup(List<OrderDTO> orderDTOs);

    ResponseEntity<Map<String, Map<String, List<OrderDTO>>>> findAllOrders();

    ResponseEntity<OrderDTO> findOrder(Long orderId);

    ResponseEntity<List<OrderDTO>> findAllOrdersFromAdmin();

    ResponseEntity<OrderDTO> getOrderFromAdmin(Long orderId);

    ResponseEntity<String> updateStatusWithSup(BulkOrderStatusUpdateDTO updateDTO);

    ResponseEntity<String> bulkUpdateOrderStatus(String orderCode, String status);

    ResponseEntity<OrderWithItemsDTO> findOrderByCode(String orderCode);

    ResponseEntity<String> updateStatusSingle(Long orderId, Long itemId, String status);

    ResponseEntity<String> authUpdatStatus(String orderId, String itemId, String status);

    ResponseEntity<List<OrderWithItemsDTO>> findPastOrders();

    ResponseEntity<String> authUpdatStatusPAYED(String orderId, String itemId, String status);
}
