package com.traveler.core.controller;

import com.traveler.core.config.TenantContext;
import com.traveler.core.repository.TripRepository;
import com.traveler.common.entity.Trip;
import com.traveler.common.dto.TripDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CoreController {
    
    @Autowired
    private TripRepository tripRepository;
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok("Core service accessed for tenant: " + tenantId);
    }
    
    @PostMapping("/trips")
    public ResponseEntity<Trip> createTrip(@RequestBody TripDto tripDto) {
        String tenantId = TenantContext.getCurrentTenant();
        
        Trip trip = new Trip(
            tripDto.getDestination(),
            tripDto.getStartDate(),
            tripDto.getEndDate(),
            tripDto.getDescription(),
            tripDto.getUserId()
        );
        
        Trip savedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(savedTrip);
    }
    
    @GetMapping("/trips")
    public ResponseEntity<List<Trip>> getTrips() {
        String tenantId = TenantContext.getCurrentTenant();
        List<Trip> trips = tripRepository.findAll();
        return ResponseEntity.ok(trips);
    }
}