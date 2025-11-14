package com.traveler.auth.traveler.dto;

import lombok.Data;

@Data
public class BankDetailsDto {
    private String accountNumber;
    private String holderName;
    private String bank;
    private String branch;
}