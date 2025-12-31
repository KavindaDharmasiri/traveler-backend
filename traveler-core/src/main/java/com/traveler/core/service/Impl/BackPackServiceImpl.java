package com.traveler.core.service.Impl;

import com.traveler.common.dto.BackpackDTO;
import com.traveler.common.dto.BackpackResponseDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.entity.Backpack;
import com.traveler.core.repository.BackpackRepository;
import com.traveler.core.service.BackPackService;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BackPackServiceImpl implements BackPackService {

    private final BackpackRepository backpackRepository;
    private final AuthClient authClient;

    public BackPackServiceImpl(BackpackRepository backpackRepository, AuthClient authClient) {
        this.backpackRepository = backpackRepository;
        this.authClient = authClient;
    }

    @Override
    public ResponseEntity<String> addToBackpack(BackpackDTO backpackDTO) {
        try {
            // Log the incoming request
            System.out.println("Received backpack request: " + backpackDTO);
            
            // Validate required fields
            if (backpackDTO.getItemId() == null) {
                return ResponseEntity.badRequest().body("Item ID is required");
            }
            if (backpackDTO.getProviderTenant() == null || backpackDTO.getProviderTenant().isEmpty()) {
                return ResponseEntity.badRequest().body("Provider tenant is required");
            }
            if (backpackDTO.getRentalDays() == null || backpackDTO.getRentalDays() <= 0) {
                return ResponseEntity.badRequest().body("Rental days must be greater than 0");
            }
            if (backpackDTO.getPickupDate() == null) {
                return ResponseEntity.badRequest().body("Pickup date is required");
            }
            if (backpackDTO.getReturnDate() == null) {
                return ResponseEntity.badRequest().body("Return date is required");
            }
            if (backpackDTO.getTotalPrice() == null) {
                return ResponseEntity.badRequest().body("Total price is required");
            }
            
            Backpack backpack = new Backpack();
            backpack.setItemId(backpackDTO.getItemId());
            backpack.setProviderTenant(backpackDTO.getProviderTenant());
            backpack.setRentalDays(backpackDTO.getRentalDays());
            backpack.setPickupDate(backpackDTO.getPickupDate());
            backpack.setReturnDate(backpackDTO.getReturnDate());
            backpack.setTotalPrice(backpackDTO.getTotalPrice());
            backpack.setQty(backpackDTO.getQuantity());
            backpack.setCode(UUID.randomUUID().toString());
            backpack.setCreatedAt(LocalDateTime.now());

            backpackRepository.save(backpack);
            
            return ResponseEntity.ok("Item added to backpack successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to add item to backpack: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<BackpackResponseDTO>> getAllBackpacks() {
        try {
            List<Backpack> backpacks = backpackRepository.findAll();
            List<BackpackResponseDTO> response = backpacks.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<String> deleteFromBackpack(Long id) {
        try {
            if (!backpackRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Backpack item not found");
            }
            backpackRepository.deleteById(id);
            return ResponseEntity.ok("Item removed from backpack successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to remove item from backpack: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> requestAllItems() {
        return null;
    }

    private BackpackResponseDTO convertToResponseDTO(Backpack backpack) {
        BackpackResponseDTO dto = new BackpackResponseDTO();
        dto.setId(backpack.getId());
        dto.setItemId(backpack.getItemId());
        dto.setProviderTenant(authClient.getUserByTenant(backpack.getProviderTenant()).getName());
        dto.setRentalDays(backpack.getRentalDays());
        dto.setQty(backpack.getQty());
        dto.setPickupDate(backpack.getPickupDate());
        dto.setReturnDate(backpack.getReturnDate());
        dto.setTotalPrice(backpack.getTotalPrice());
        dto.setCreatedAt(backpack.getCreatedAt());
        
        // Get item details
        try {
            ItemDTO itemDTO = authClient.getItemForTraveler(backpack.getItemId(), backpack.getProviderTenant()).getBody();
            dto.setItem(itemDTO);
        } catch (Exception e) {
            dto.setItem(null);
        }
        
        return dto;
    }
}
