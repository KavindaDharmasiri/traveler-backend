package com.traveler.core.service;

import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

public interface ProviderItemService {

    ResponseEntity<String> createItem(ItemDTO itemDTO);

    ResponseEntity<List<ItemDTO>> getItems();

    ResponseEntity<ItemDTO> getItem(Long itemId);

    ResponseEntity<String> updateItem(ItemDTO itemDTO);

    ResponseEntity<String> deleteItem(Long itemId);

    ResponseEntity<Map<String, Object>> getItemsForTraveller(int page, int size, String category, String provider, Double minPrice, Double maxPrice, Double minRating);
    
    ResponseEntity<Map<String, Object>> getFilters();

    List<ItemDetailsDTO> getItemsForTravellers();

    ResponseEntity<ItemDTO> getItemforTraveller(Long itemId, String tenant);

    ResponseEntity<List<ItemDTO>> getItemsWithStatus(String status);

    ResponseEntity<String> updateItemStatus(String status, long id);
}
