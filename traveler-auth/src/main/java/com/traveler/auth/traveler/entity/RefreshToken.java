package com.traveler.auth.traveler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false , name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(nullable = false,name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
