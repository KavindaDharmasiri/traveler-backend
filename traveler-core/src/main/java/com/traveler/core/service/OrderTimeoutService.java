package com.traveler.core.service;

import com.traveler.common.dto.BulkOrderStatusUpdateDTO;
import com.traveler.common.entity.Order;
import com.traveler.common.utils.STATUS;
import com.traveler.core.repository.OrderRepository;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderTimeoutService {

    private final OrderRepository orderRepository;
    private final AuthClient authClient;

    public OrderTimeoutService(OrderRepository orderRepository, AuthClient authClient) {
        this.orderRepository = orderRepository;
        this.authClient = authClient;
    }

    @Scheduled(fixedRate = 1800000)
    public void checkExpiredOrders() {
        try {
            List<Order> pendingOrders = orderRepository.findAllByStatusNot(STATUS.DELETED).stream()
                .filter(o -> o.getStatus() == STATUS.PENDING)
                .collect(Collectors.toList());

            LocalDateTime now = LocalDateTime.now();
            List<String> expiredOrderCodes = pendingOrders.stream()
                .filter(order -> isExpired(order.getCreatedAt(), now))
                .map(Order::getOrderCode)
                .collect(Collectors.toList());

            if (!expiredOrderCodes.isEmpty()) {
                BulkOrderStatusUpdateDTO updateDTO = new BulkOrderStatusUpdateDTO();
                updateDTO.setOrderCodes(expiredOrderCodes);
                updateDTO.setStatus(STATUS.CANCELLED);
                authClient.updateOrderStatus(updateDTO);
            }
        } catch (Exception e) {
            System.err.println("Order timeout check error: " + e.getMessage());
        }
    }

    private boolean isExpired(Date createdAt, LocalDateTime now) {
        LocalDateTime createdTime = new java.sql.Timestamp(createdAt.getTime()).toLocalDateTime();
        LocalTime createdLocalTime = createdTime.toLocalTime();
        boolean isDaytime = createdLocalTime.isAfter(LocalTime.of(6, 0)) && createdLocalTime.isBefore(LocalTime.of(22, 0));
        int timeoutHours = isDaytime ? 1 : 3;
        return Duration.between(createdTime, now).toHours() >= timeoutHours;
    }
}
