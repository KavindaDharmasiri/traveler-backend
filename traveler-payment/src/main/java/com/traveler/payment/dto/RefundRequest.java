package com.traveler.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefundRequest {
    
    @NotBlank(message = "Payment ID is required")
    private String paymentId;
    
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal amount; // null for full refund
    
    private String reason;
}