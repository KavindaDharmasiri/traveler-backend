package com.traveler.auth.traveler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDTO {
    private String name;
    private String gender;
    private String googleMapsUrl;
    private String contactNumber;
    private String tenantId;
    private String email;
    private String country;
    private String profileImageUuid;
    private String dateOfBirth;
    private String nicNumber;
    private String nicImageUuid;
    private AddressDto address;
    private BankDetailsDto bankDetails;

    @Override
    public String toString() {
        return "ProfileUpdateDTO{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", googleMapsUrl='" + googleMapsUrl + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", email='" + email + '\'' +
                ", country='" + country + '\'' +
                ", profileImageUuid='" + profileImageUuid + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", nicNumber='" + nicNumber + '\'' +
                ", nicImageUuid='" + nicImageUuid + '\'' +
                ", address=" + address +
                ", bankDetails=" + bankDetails +
                '}';
    }
}
