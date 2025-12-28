package com.traveler.common.dto;

import com.traveler.common.dto.provider.ItemDTO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BackpackResponseDTO {
    private Long id;
    private Long itemId;
    private String providerTenant;
    private Integer rentalDays;
    private Integer qty;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private ItemDTO item;
}
