package com.traveler.core.service.Impl;

import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.UserResponse;
import com.traveler.common.entity.Order;
import com.traveler.common.utils.STATUS;
import com.traveler.core.config.TenantContext;
import com.traveler.core.controller.CoreController;
import com.traveler.core.repository.OrderRepository;
import com.traveler.core.service.OrderService;
import com.traveler.core.service.TenantOrderService;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

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
    private final CoreController coreController;

    public OrderServiceImpl(TenantOrderService tenantOrderService, OrderRepository orderRepository, AuthClient authClient, CoreController coreController) {
        this.tenantOrderService = tenantOrderService;
        this.orderRepository = orderRepository;
        this.authClient = authClient;
        this.coreController = coreController;
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
    public ResponseEntity<Map<String, Map<String, List<OrderDTO>>>> findAllOrders() {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            System.out.println("Current tenant: " + currentTenant);
            var currentUser = authClient.getUserByTenant(currentTenant);
            boolean isServiceProvider = "SERVICE_PROVIDER".equals(currentUser.getType());

            List<Order> all = orderRepository.findAllByStatusNot(STATUS.DELETED);

            Map<String, Map<String, List<OrderDTO>>> groupedOrders = all.stream()
                    .map(order -> {
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

                        String groupTenant = isServiceProvider ? order.getClientTenant() : order.getProviderTenant();
                        var tenantUser = authClient.getUserByTenant(groupTenant);
                        String groupName = tenantUser.getName();

                        orderDTO.setGroupTenant(groupTenant);
                        orderDTO.setGroupName(groupName);

                        return orderDTO;
                    })
                    .collect(Collectors.groupingBy(
                            OrderDTO::getGroupTenant,
                            Collectors.groupingBy(OrderDTO::getGroupName)
                    ));

            return ResponseEntity.ok(groupedOrders);
        } catch (Exception e) {
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
