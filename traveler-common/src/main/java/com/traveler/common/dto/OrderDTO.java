package com.traveler.common.dto;

import com.traveler.common.dto.provider.ItemDTO;
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
    private ItemDTO itemObj;
    private STATUS status;
    private double totalPrice;
    private int rentalDays;
    private int qty;
    private String clientTenant;
    private String providerTenant;
    private String groupTenant;
    private String groupName;
    private String pickupDate;
    private String returnDate;
    private String providerName;
    private String map;

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

    public ItemDTO getItemObj() {
        return itemObj;
    }

    public void setItemObj(ItemDTO itemObj) {
        this.itemObj = itemObj;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
