package com.traveler.common.dto;

import com.traveler.common.dto.provider.ItemDTO;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CartItemDTO {
    private Long id;
    private int item;
    private int qty;
    private double totalPrice;
    private int rentalDays;
    private String providerTenant;
    private String providerName;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private ItemDTO itemObj;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
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

    public String getProviderTenant() {
        return providerTenant;
    }

    public void setProviderTenant(String providerTenant) {
        this.providerTenant = providerTenant;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public ItemDTO getItemObj() {
        return itemObj;
    }

    public void setItemObj(ItemDTO itemObj) {
        this.itemObj = itemObj;
    }
}
