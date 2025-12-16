package com.traveler.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProviderBankDetails {
    private String providerId;
    private String providerName;
    private String bankName;
    private String bankCode;
    private String accountNumber;
    private String accountHolderName;
    private String branchCode;
    private BigDecimal amount;
    private BigDecimal commissionRate;
}