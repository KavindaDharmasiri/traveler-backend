package com.traveler.core.service;

import com.traveler.common.dto.CartDTO;
import com.traveler.common.dto.CartItemDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.entity.Cart;
import com.traveler.core.config.TenantContext;
import com.traveler.core.repository.CartItemsRepository;
import com.traveler.core.repository.CartRepository;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final AuthClient authClient;

    public CartService(CartRepository cartRepository, CartItemsRepository cartItemsRepository, AuthClient authClient) {
        this.cartRepository = cartRepository;
        this.cartItemsRepository = cartItemsRepository;
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
            dto.setOrderCode(cart.getOrderCode());
            
            // Set cart items
            if (cart.getCartItems() != null) {
                List<CartItemDTO> cartItemDTOs = cart.getCartItems().stream()
                    .map(cartItem -> {
                        CartItemDTO itemDTO = new CartItemDTO();
                        itemDTO.setId(cartItem.getId());
                        itemDTO.setItem(cartItem.getItem());
                        itemDTO.setQty(cartItem.getQty());
                        itemDTO.setRentalDays(cartItem.getRentalDays());
                        itemDTO.setPickupDate(cartItem.getPickupDate());
                        itemDTO.setReturnDate(cartItem.getReturnDate());
                        itemDTO.setTotalPrice(cartItem.getTotalPrice());
                        itemDTO.setProviderName(authClient.getUserByTenant(cartItem.getProviderTenant()).getName());
                        ResponseEntity<ItemDTO> itemForTraveler = authClient.getItemForTraveler(Long.valueOf(cartItem.getItem()), cartItem.getProviderTenant());
                        itemDTO.setItemObj(itemForTraveler.getBody());
                        itemDTO.setProviderTenant(cartItem.getProviderTenant());
                        return itemDTO;
                    })
                    .collect(Collectors.toList());
                dto.setCartItems(cartItemDTOs);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    public Long getCartCount() {
        try {
            return cartItemsRepository.count();
        }catch (Exception e){
            System.err.println("Error fetching cart count: " + e.getMessage());
            return 0L;
        }
    }
}
