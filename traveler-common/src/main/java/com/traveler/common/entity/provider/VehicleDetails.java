package com.traveler.common.entity.provider;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicle_details")
@Data
public class VehicleDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vehicle_number")
    private String vehicleNumber;
    
    @Column(name = "passenger_count")
    private Integer passengerCount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_condition")
    private Condition condition;
    
    @Column(name = "km_per_day")
    private Double kmPerDay;
    
    @Column(name = "price_per_extra_km")
    private Double pricePerExtraKm;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "driver_status")
    private DriverStatus driverStatus;
    
    @Column(name = "waiting_charge_per_night")
    private Double waitingChargePerNight;
    
    public enum Condition {
        AC, NON_AC
    }
    
    public enum DriverStatus {
        WITH_DRIVER, WITHOUT_DRIVER, BOTH
    }
}
