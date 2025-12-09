package com.traveler.core.service;

import com.traveler.common.dto.CartDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.entity.Cart;
import com.traveler.core.config.TenantContext;
import com.traveler.core.repository.CartRepository;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final AuthClient authClient;

    public CartService(CartRepository cartRepository, AuthClient authClient) {
        this.cartRepository = cartRepository;
        this.authClient = authClient;
    }

    public List<CartDTO> getCartItems() {
        String tenantId = TenantContext.getCurrentTenant();
        List<Cart> carts = cartRepository.findAll().stream()
            .filter(c -> c.getUserTenant().equals(tenantId))
            .collect(Collectors.toList());

        return carts.stream().map(cart -> {
            CartDTO dto = new CartDTO();
            dto.setId(cart.getId());
            dto.setUserTenant(cart.getUserTenant());
            
            if (cart.getOrder() != null) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(cart.getOrder().getId());
                orderDTO.setOrderCode(cart.getOrder().getOrderCode());
                orderDTO.setCustomerName(cart.getOrder().getCustomerName());
                orderDTO.setItem(cart.getOrder().getItem());
                orderDTO.setStatus(cart.getOrder().getStatus());
                orderDTO.setTotalPrice(cart.getOrder().getTotalPrice());
                orderDTO.setRentalDays(cart.getOrder().getRentalDays());
                orderDTO.setProviderTenant(cart.getOrder().getProviderTenant());
                dto.setOrder(orderDTO);

                try {
                    ItemDTO itemDTO = authClient.getItemForTraveler(
                        Long.valueOf(cart.getOrder().getItem()),
                        cart.getOrder().getProviderTenant()
                    ).getBody();
                    dto.setItemDetails(itemDTO);
                } catch (Exception e) {
                    System.err.println("Failed to fetch item details: " + e.getMessage());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}
