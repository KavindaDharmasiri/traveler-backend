package com.traveler.auth.traveler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tax")
public class TaxController {

    @GetMapping("/rate")
    public ResponseEntity<Map<String, Object>> getTaxRate() {
        Map<String, Object> response = new HashMap<>();
        response.put("rate", 0.08);
        response.put("type", "PERCENTAGE");
        return ResponseEntity.ok(response);
    }
}
