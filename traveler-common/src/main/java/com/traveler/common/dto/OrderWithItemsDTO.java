package com.traveler.common.dto;

import java.util.List;

public class OrderWithItemsDTO {
    private Long id;
    private String orderCode;
    private String customerName;
    private String status;
    private String clientTenant;
    private String groupTenant;
    private String groupName;
    private List<OrderDTO> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientTenant() {
        return clientTenant;
    }

    public void setClientTenant(String clientTenant) {
        this.clientTenant = clientTenant;
    }

    public String getGroupTenant() {
        return groupTenant;
    }

    public void setGroupTenant(String groupTenant) {
        this.groupTenant = groupTenant;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<OrderDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderDTO> items) {
        this.items = items;
    }
}