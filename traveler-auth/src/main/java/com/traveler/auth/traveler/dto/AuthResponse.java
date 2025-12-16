package com.traveler.auth.traveler.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String name;
    private String type;
    private String tenantId;
    private String country;
    private boolean numberVerified;
    private boolean emailVerified;
}
