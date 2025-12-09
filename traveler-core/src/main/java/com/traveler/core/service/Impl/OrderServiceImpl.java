package com.traveler.core.service.Impl;

import com.traveler.common.dto.BulkOrderStatusUpdateDTO;
import com.traveler.common.dto.NotificationDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.entity.Cart;
import com.traveler.common.entity.Order;
import com.traveler.common.utils.STATUS;
import com.traveler.core.config.TenantContext;
import com.traveler.core.controller.CoreController;
import com.traveler.core.repository.CartRepository;
import com.traveler.core.repository.OrderRepository;
import com.traveler.core.service.OrderService;
import com.traveler.core.service.OrderTimeoutService;
import com.traveler.core.service.TenantOrderService;
import com.traveler.core.service.feign.AuthClient;
import com.traveler.core.service.feign.NotificationClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final NotificationClient notificationClient;
    private final CartRepository cartRepository;

    public OrderServiceImpl(TenantOrderService tenantOrderService, OrderRepository orderRepository, AuthClient authClient, CoreController coreController, NotificationClient notificationClient, CartRepository cartRepository) {
        this.tenantOrderService = tenantOrderService;
        this.orderRepository = orderRepository;
        this.authClient = authClient;
        this.coreController = coreController;
        this.notificationClient = notificationClient;
        this.cartRepository = cartRepository;
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

            // Send notification to provider
            try {
                NotificationDTO notification = new NotificationDTO();
                if (TenantContext.getCurrentTenant().equals(orderDTO.getClientTenant())) {
                    notification.setSenderTenant(orderDTO.getProviderTenant());
                    notification.setReceiverTenant(orderDTO.getClientTenant());
                    notification.setTitle("New Order Request");
                    notification.setMessage("You have Created a new order request");
                } else {
                    notification.setSenderTenant(orderDTO.getClientTenant());
                    notification.setReceiverTenant(orderDTO.getProviderTenant());
                    notification.setTitle("New Order Request");
                    notification.setMessage("You have a new order request from " + orderDTO.getCustomerName());
                }
                notification.setNotificationType("ORDER_REQUEST");
                notification.setReferenceId(order.getOrderCode());
                notificationClient.sendNotification(notification);
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }

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
        } catch (Exception e) {
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

            Map<String, Map<String, List<OrderDTO>>> groupedOrders = all.stream().map(order -> {
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
            }).collect(Collectors.groupingBy(OrderDTO::getGroupTenant, Collectors.groupingBy(OrderDTO::getGroupName)));

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
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<List<OrderDTO>> findAllOrdersFromAdmin() {
        try {
            return authClient.getOrders();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<OrderDTO> getOrderFromAdmin(Long orderId) {
        try {
            return authClient.getOrder(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<String> updateStatusWithSup(BulkOrderStatusUpdateDTO updateDTO) {
        try {
            return ResponseEntity.ok(authClient.updateOrderStatus(updateDTO));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update orders");
        }
    }

    @Override
    public ResponseEntity<String> bulkUpdateOrderStatus(String orderCode, String status) {
        try {
            Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus(STATUS.valueOf(status));
                orderRepository.save(order);

                NotificationDTO notification = new NotificationDTO();

                if (authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType().name().equals("SERVICE_PROVIDER")) {
                    System.out.println("1");
                    notification.setSenderTenant(order.getProviderTenant());
                    notification.setReceiverTenant(order.getProviderTenant());
                } else {
                    System.out.println("2");
                    notification.setSenderTenant(order.getProviderTenant());
                    notification.setReceiverTenant(order.getClientTenant());

                    if (status.equals("ACCEPTED")) {
                        this.createCartForUser(orderCode);
                    }
                }
//                notification.setSenderTenant(TenantContext.getCurrentTenant());
//                notification.setReceiverTenant(order.getClientTenant());
                notification.setMessage("Your order status has been updated to " + status);
                if (status.equals("CANCELLED")) {
                    notification.setMessage("Your order has been cancelled");
                    notification.setNotificationType("ORDER_REJECTED");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                } else if (status.equals("COMPLETED")) {
                    notification.setMessage("Your order has been completed");
                    notification.setNotificationType("ORDER_COMPLETE");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                } else if (status.equals("ACCEPTED")) {
                    notification.setMessage("Your order has been completed");
                    notification.setNotificationType("ORDER_ACCEPTED");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                } else if (status.equals("PAYED")) {
                    notification.setMessage("Your order has been completed");
                    notification.setNotificationType("ORDER_PAYED");
                    notification.setTitle("Order "+status+" By "+order.getCustomerName());
                } else {
                    notification.setNotificationType("ORDER_UPDATE");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                }
                notification.setReferenceId(order.getOrderCode());
                notificationClient.sendNotification(notification);

                return ResponseEntity.ok("Order updated successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update order");
        }
    }

    private void createCartForUser(String orderCode) {
        try {
            Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
            if (order != null) {
                Cart cart = new Cart();
                cart.setUserTenant(order.getClientTenant());
                cart.setOrder(order);
                cartRepository.save(cart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
