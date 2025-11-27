package com.traveler.core.controller;

import com.traveler.common.dto.TripDto;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.entity.Trip;
import com.traveler.core.config.TenantContext;
import com.traveler.core.repository.TripRepository;
import com.traveler.core.service.ProviderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/provider/item")
public class providerItemController {
    
    @Autowired
    private ProviderItemService providerItemService;

    @PostMapping()
    public ResponseEntity<String> createItem(@RequestBody ItemDTO itemDTO) {
        return providerItemService.createItem(itemDTO);
    }

    @PutMapping()
    public ResponseEntity<String> updateItem(@RequestBody ItemDTO itemDTO) {
        return providerItemService.updateItem(itemDTO);
    }

    @GetMapping()
    public ResponseEntity<List<ItemDTO>> getItems() {
        return providerItemService.getItems();
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable Long itemId) {
        return providerItemService.getItem(itemId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        return providerItemService.deleteItem(itemId);
    }

}
