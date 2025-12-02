package com.traveler.common.dto.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDetailsDTO {
    private String address;
    private String roomNumber;
    private Integer maxGuests;
}