package com.traveler.core.controller;

import com.traveler.common.dto.UserResponse;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuthClient authClient;

    public AdminController(AuthClient authClient) {
        this.authClient = authClient;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return authClient.getAllUsers();
    }

    @GetMapping("/users/{type}")
    public ResponseEntity<List<UserResponse>> getUsersByType(@PathVariable String type) {
        return authClient.getUsersByType(type);
    }
}