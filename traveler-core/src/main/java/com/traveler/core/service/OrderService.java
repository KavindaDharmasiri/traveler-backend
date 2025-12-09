package com.traveler.core.service;

import com.traveler.common.dto.OrderDTO;
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
    ResponseEntity<String> createOrder(OrderDTO orderDTO);

    ResponseEntity<String> createOrderWithSup(OrderDTO orderDTO);

    ResponseEntity<Map<String, Map<String, List<OrderDTO>>>> findAllOrders();

    ResponseEntity<OrderDTO> findOrder(Long orderId);

    ResponseEntity<List<OrderDTO>> findAllOrdersFromAdmin();

    ResponseEntity<OrderDTO> getOrderFromAdmin(Long orderId);
}
