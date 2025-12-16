package com.traveler.core.controller;

import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import com.traveler.core.service.ProviderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


    /////for traveller
//api for postman
    @GetMapping("/getAllForTraveller")
    public ResponseEntity<Map<String, Object>> getItemsForTravellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "36") int size) {
        return providerItemService.getItemsForTraveller(page, size);
    }

    //api for feign auth client
    @GetMapping("/getItemsForTraveller")
    public List<ItemDetailsDTO> getItemsForTraveller() {
        return providerItemService.getItemsForTravellers();
    }

    @GetMapping("/{itemId}/{tenant}")
    public ResponseEntity<ItemDTO> getItem(
            @PathVariable Long itemId,
            @PathVariable String tenant) {

        return providerItemService.getItemforTraveller(itemId, tenant);
    }

}
