package com.traveler.core.service;

import com.traveler.common.dto.OrderDTO;
import com.traveler.common.entity.Order;
import com.traveler.common.utils.STATUS;
import com.traveler.core.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantOrderService {
    
    private final OrderRepository orderRepository;
    private final TenantSwitchingService tenantSwitchingService;
    
    public TenantOrderService(OrderRepository orderRepository, TenantSwitchingService tenantSwitchingService) {
        this.orderRepository = orderRepository;
        this.tenantSwitchingService = tenantSwitchingService;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveInTenant(String tenantId, OrderDTO orderDTO, String orderType) {
        return tenantSwitchingService.executeInTenant(tenantId, () -> {
            System.out.println("=== " + orderType + " SAVE ===");
            System.out.println("Current tenant: " + com.traveler.core.config.TenantContext.getCurrentTenant());
            
            Order order = new Order();
            order.setOrderCode("ORD-" + orderType + "-" + System.currentTimeMillis());
            order.setCustomerName(orderDTO.getCustomerName());
            order.setItem(orderDTO.getItem());
            order.setStatus(STATUS.PENDING);
            order.setTotalPrice(orderDTO.getTotalPrice());
            order.setRentalDays(orderDTO.getRentalDays());
            order.setClientTenant(orderDTO.getClientTenant());
            order.setProviderTenant(orderDTO.getProviderTenant());
            
            Order saved = orderRepository.save(order);
            System.out.println(orderType + " order saved with ID: " + saved.getId());
            return saved;
        });
    }
}