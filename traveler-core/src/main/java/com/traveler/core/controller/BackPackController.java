package com.traveler.core.controller;

import com.traveler.common.dto.BackpackDTO;
import com.traveler.common.dto.BackpackResponseDTO;
import com.traveler.common.entity.Backpack;
import com.traveler.core.service.BackPackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/backpack")
public class BackPackController {

    private final BackPackService backPackService;

    public BackPackController(BackPackService backPackService) {
        this.backPackService = backPackService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToBackpack(@RequestBody BackpackDTO backpackDTO) {
        return backPackService.addToBackpack(backpackDTO);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BackpackResponseDTO>> getAllBackpacks() {
        return backPackService.getAllBackpacks();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFromBackpack(@PathVariable Long id) {
        return backPackService.deleteFromBackpack(id);
    }

    @PostMapping("/request-all")
    public ResponseEntity<String> requestAllItems() {
        return backPackService.requestAllItems();
    }
}
