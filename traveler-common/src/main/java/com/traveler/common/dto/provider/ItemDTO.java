package com.traveler.common.dto.provider;

import com.traveler.common.utils.STATUS;
import lombok.*;

import java.util.List;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private String category;
    private String name;
    private Long id;
    private String contact;
    private String tenant;
    private String tenantName;
    private int qty;
    private String description;
    private String currency;
    private STATUS status;
    private List<String> images;
    private double pricePerDay;
    private VehicleDetailsDTO vehicleDetails;
    private HotelDetailsDTO hotelDetails;
    private Double OverallRating;

    private List<ItemReviewDTO> reviews;
}
