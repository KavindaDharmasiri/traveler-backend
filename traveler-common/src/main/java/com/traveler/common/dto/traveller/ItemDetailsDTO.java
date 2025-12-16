package com.traveler.common.dto.traveller;

import com.traveler.common.dto.provider.HotelDetailsDTO;
import com.traveler.common.dto.provider.VehicleDetailsDTO;
import com.traveler.common.utils.STATUS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailsDTO {
    private Long id;
    private String category;
    private String name;
    private String contact;
    private String description;
    private String currency;
    private String tenant;
    private String providerName;
    private STATUS status;
    private List<String> images;
    private double pricePerDay;
    private Double overallRating;
    private VehicleDetailsDTO vehicleDetails;
    private HotelDetailsDTO hotelDetails;

}
