package com.traveler.core.service;

import com.traveler.common.dto.provider.ItemDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
}
