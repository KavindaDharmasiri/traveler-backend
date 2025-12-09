package com.traveler.common.dto;


import com.traveler.common.utils.UserType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private UserType type;
    private String name;
    private String gender;
    private String contactNumber;
    private Boolean isNumberVerified;
    private String email;
    private Boolean isEmailVerified;
    private LocalDate dateOfBirth;
    private String nicNumber;
    private String nicImageUuid;
    private String tenantId;
}
