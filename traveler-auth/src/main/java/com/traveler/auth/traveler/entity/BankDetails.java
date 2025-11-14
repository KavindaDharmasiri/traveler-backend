package com.traveler.auth.traveler.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bank_details")
@Data
public class BankDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;
    @Column(name = "holder_name", nullable = false)
    private String holderName;
    private String bank;
    private String branch;
}
