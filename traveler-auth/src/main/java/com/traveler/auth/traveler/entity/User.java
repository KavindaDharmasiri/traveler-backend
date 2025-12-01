package com.traveler.auth.traveler.entity;

import com.traveler.auth.traveler.utils.UserType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;
    
    @Column(nullable = false)
    private String name;
    
    private String gender;

//    private String uniqIdentifier;

    @Column(nullable = false ,name = "contact_number", unique = true)
    private String contactNumber;
    
    @Column(nullable = false, name = "is_number_verified")
    private Boolean isNumberVerified = false;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false,name = "is_email_verified")
    private Boolean isEmailVerified = false;
    
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String country;

    @Column(name = "profile_image_uuid")
    private String profileImageUuid;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nic_number", unique = true)
    private String nicNumber;

    @Column(name = "nic_image_uuid")
    private String nicImageUuid;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bank_details_id")
    private BankDetails bankDetails;
    
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;
    
    @Column(nullable = false, name = "tenant_id", unique = true)
    private String tenantId;
}
