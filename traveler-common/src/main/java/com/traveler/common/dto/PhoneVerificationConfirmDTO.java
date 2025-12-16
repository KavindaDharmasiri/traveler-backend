package com.traveler.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneVerificationConfirmDTO {
    private String phoneNumber;
    private String pin;
}