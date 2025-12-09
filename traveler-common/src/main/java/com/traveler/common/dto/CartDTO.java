package com.traveler.common.dto;

import com.traveler.common.dto.provider.ItemDTO;

public class CartDTO {
    private Long id;
    private String userTenant;
    private OrderDTO order;
    private ItemDTO itemDetails;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserTenant() {
        return userTenant;
    }

    public void setUserTenant(String userTenant) {
        this.userTenant = userTenant;
    }

    public OrderDTO getOrder() {
        return order;
    }

    public void setOrder(OrderDTO order) {
        this.order = order;
    }

    public ItemDTO getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(ItemDTO itemDetails) {
        this.itemDetails = itemDetails;
    }
}
