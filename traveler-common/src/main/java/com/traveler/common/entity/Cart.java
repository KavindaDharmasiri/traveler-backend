package com.traveler.common.entity;

import com.traveler.common.utils.STATUS;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "cart")
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_tenant", nullable = false)
    private String userTenant;

    @Column(name = "order_code", nullable = false)
    private String orderCode;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItems> cartItems;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "customer_name", nullable = false)
    private String customerName;
    private int item;
    private int qty;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date updatedAt;
    private STATUS status;
    @Column(name = "rental")
    private double totalPrice;
    @Column(name = "rental_days")
    private int rentalDays;
    @Column(name = "client_tenant")
    private String clientTenant;
    @Column(name = "provider_tenant")
    private String providerTenant;
    @Column(name = "pickup_date")
    private LocalDate pickupDate;
    @Column(name = "return_date")
    private LocalDate returnDate;

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

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public List<CartItems> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItems> cartItems) {
        this.cartItems = cartItems;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
