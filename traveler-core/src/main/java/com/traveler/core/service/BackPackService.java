package com.traveler.core.service;

import com.traveler.common.dto.BackpackDTO;
import com.traveler.common.dto.BackpackResponseDTO;
import com.traveler.common.entity.Backpack;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface BackPackService {
    ResponseEntity<String> addToBackpack(BackpackDTO backpackDTO);
    ResponseEntity<List<BackpackResponseDTO>> getAllBackpacks();
    ResponseEntity<String> deleteFromBackpack(Long id);
    ResponseEntity<String> requestAllItems();
}
