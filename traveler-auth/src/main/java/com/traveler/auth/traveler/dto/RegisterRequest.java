package com.traveler.auth.traveler.dto;

import com.traveler.auth.traveler.utils.UserType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.Pattern;

@Data
public class RegisterRequest {
    @NotNull
    private UserType type;
    
    @NotBlank
    private String name;
    
    private String gender;

    private String tenant;
    private String country;

    @NotBlank
    private String contactNumber;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$", 
             message = "Password must contain uppercase, lowercase, number and special character")
    private String password;
    
    private LocalDate dateOfBirth;
    
    private String nicNumber;
    
    private String nicImageUuid;
    private String nicBackUuid;
    private String googleMapsUrl;

    @NotNull
    private AddressDto address;
    
    private BankDetailsDto bankDetails;
    
    private java.util.Map<String, Object> permissions; // Add permissions field
    
    private List<DocumentDto> documents; // Documents for providers
    
    @Data
    public static class DocumentDto {
        private String docName;
        private String docUuid;
    }
}
