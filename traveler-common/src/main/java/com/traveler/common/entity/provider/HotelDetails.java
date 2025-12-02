package com.traveler.common.entity.provider;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "hotel_details")
@Data
public class HotelDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String address;
    
    @Column(name = "room_number")
    private String roomNumber;
    
    @Column(name = "max_guests")
    private Integer maxGuests;
}