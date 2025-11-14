package com.traveler.auth.traveler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Data
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private Boolean successful;
    
    @Column(nullable = false)
    private LocalDateTime attemptTime = LocalDateTime.now();
}