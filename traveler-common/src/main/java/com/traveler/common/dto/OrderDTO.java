package com.traveler.common.dto;

import com.traveler.common.entity.provider.Item;
import com.traveler.common.utils.STATUS;
import jakarta.persistence.*;

import java.util.Date;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

public class OrderDTO {
    private Long id;
    private String orderCode;
    private String customerName;
    private int item;
    private STATUS status;
    private double totalPrice;
    private int rentalDays;
    private String clientTenant;
    private String providerTenant;

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

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public void setRentalDays(int rentalDays) {
        this.rentalDays = rentalDays;
    }

    public String getClientTenant() {
        return clientTenant;
    }

    public void setClientTenant(String clientTenant) {
        this.clientTenant = clientTenant;
    }

    public String getProviderTenant() {
        return providerTenant;
    }

    public void setProviderTenant(String providerTenant) {
        this.providerTenant = providerTenant;
    }

}
