package com.traveler.auth.traveler.dto;

import com.traveler.auth.traveler.utils.UserType;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

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
    private String nicImageBackUuid;
    private String tenantId;
    private String googleMapsUrl;
    private String country;
    private String profileImageUuid;
    private Boolean isActive;
    private AddressDto address;
    private BankDetailsDto bankDetails;
    private List<DocumentDto> documents;
    
    @Data
    public static class DocumentDto {
        private String docName;
        private String docUuid;
    }
}
