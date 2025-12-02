package com.traveler.common.dto.provider;

import com.traveler.common.entity.provider.VehicleDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailsDTO {
    private String vehicleNumber;
    private Integer passengerCount;
    private VehicleDetails.Condition condition;
    private Double kmPerDay;
    private Double pricePerExtraKm;
    private VehicleDetails.DriverStatus driverStatus;
    private Double waitingChargePerNight;
}