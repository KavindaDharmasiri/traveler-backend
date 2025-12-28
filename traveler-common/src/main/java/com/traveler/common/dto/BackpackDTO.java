package com.traveler.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BackpackDTO {
    private Long itemId;
    private String providerTenant;
    private Integer rentalDays;
    private Integer quantity;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private BigDecimal totalPrice;
}
