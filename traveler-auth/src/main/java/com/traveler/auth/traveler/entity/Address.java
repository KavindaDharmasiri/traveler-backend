package com.traveler.auth.traveler.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "addresses")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = true)
    private String street1;

    @Column(nullable = true)
    private String street2;

    @Column(nullable = true)
    private String city;

    @Column(nullable = true)
    private String state;
    
    @Column(nullable = true,name = "postal_code")
    private String postalCode;
}
