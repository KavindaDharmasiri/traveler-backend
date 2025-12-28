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
    
    @Column(nullable = false)
    private Long itemId;
    
    @Column(nullable = false)
    private String providerTenant;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Integer rentalDays;
    private Integer qty;

    @Column(nullable = false)
    private LocalDate pickupDate;
    
    @Column(nullable = false)
    private LocalDate returnDate;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
