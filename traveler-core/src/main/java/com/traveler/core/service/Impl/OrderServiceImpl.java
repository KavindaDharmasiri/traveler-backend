package com.traveler.core.service.Impl;

import com.traveler.common.dto.OrderDTO;
import com.traveler.common.entity.Order;
import com.traveler.common.utils.STATUS;
import com.traveler.core.repository.OrderRepository;
import com.traveler.core.service.OrderService;
import com.traveler.core.service.TenantOrderService;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Service
public class OrderServiceImpl implements OrderService {

    private final TenantOrderService tenantOrderService;
    private final OrderRepository orderRepository;
    private final AuthClient authClient;

    public OrderServiceImpl(TenantOrderService tenantOrderService, OrderRepository orderRepository, AuthClient authClient) {
        this.tenantOrderService = tenantOrderService;
        this.orderRepository = orderRepository;
        this.authClient = authClient;
    }

    @Override
    public ResponseEntity<String> createOrder(OrderDTO orderDTO) {
        try {
            Optional<Order> existingOrder = orderRepository.findByOrderCode(orderDTO.getOrderCode());
            Order order;
            if (existingOrder.isPresent()) {
                order = existingOrder.get();
            } else {
                order = new Order();
            }


            order.setCustomerName(orderDTO.getCustomerName());
            order.setOrderCode(orderDTO.getOrderCode());
            order.setItem(orderDTO.getItem());
            if (orderDTO.getStatus() != null) {
                order.setStatus(orderDTO.getStatus());
            } else {
                order.setStatus(STATUS.PENDING);
            }
            order.setTotalPrice(orderDTO.getTotalPrice());
            order.setRentalDays(orderDTO.getRentalDays());
            order.setClientTenant(orderDTO.getClientTenant());
            order.setProviderTenant(orderDTO.getProviderTenant());
            orderRepository.save(order);

            return ResponseEntity.ok("Order created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create order");
        }
    }

    @Override
    public ResponseEntity<String> createOrderWithSup(OrderDTO orderDTO) {
        try {
            authClient.saveOrder(orderDTO);
            return ResponseEntity.ok("Order created successfully");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create order");
        }
    }

    @Override
    public ResponseEntity<List<OrderDTO>> findAllOrders() {
        try {
            List<Order> all = orderRepository.findAllByStatusNot(STATUS.DELETED);
            List<OrderDTO> orderDTOS = all.stream().map(order -> {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setOrderCode(order.getOrderCode());
                orderDTO.setCustomerName(order.getCustomerName());
                orderDTO.setItem(order.getItem());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setTotalPrice(order.getTotalPrice());
                orderDTO.setRentalDays(order.getRentalDays());
                orderDTO.setClientTenant(order.getClientTenant());
                orderDTO.setProviderTenant(order.getProviderTenant());
                return orderDTO;
            }).toList();
            return ResponseEntity.ok(orderDTOS);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<OrderDTO> findOrder(Long orderId) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setOrderCode(order.getOrderCode());
                orderDTO.setCustomerName(order.getCustomerName());
                orderDTO.setItem(order.getItem());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setTotalPrice(order.getTotalPrice());
                orderDTO.setRentalDays(order.getRentalDays());
                orderDTO.setClientTenant(order.getClientTenant());
                orderDTO.setProviderTenant(order.getProviderTenant());
                return ResponseEntity.ok(orderDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<List<OrderDTO>> findAllOrdersFromAdmin() {
        try {
            return authClient.getOrders();
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<OrderDTO> getOrderFromAdmin(Long orderId) {
        try {
            return authClient.getOrder(orderId);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

}
