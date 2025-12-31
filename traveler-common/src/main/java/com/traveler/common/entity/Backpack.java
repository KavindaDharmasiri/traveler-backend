package com.traveler.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "backpack")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Backpack {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "item_id")
    private Long itemId;
    
    @Column(nullable = false, name = "provider_tenant")
    private String providerTenant;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false, name = "order_code")
    private Integer rentalDays;
    private Integer qty;

    @Column(nullable = false, name = "pickup_date")
    private LocalDate pickupDate;
    
    @Column(nullable = false, name = "return_date")
    private LocalDate returnDate;
    
    @Column(nullable = false, precision = 10, scale = 2, name = "total_price")
    private BigDecimal totalPrice;
    
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
