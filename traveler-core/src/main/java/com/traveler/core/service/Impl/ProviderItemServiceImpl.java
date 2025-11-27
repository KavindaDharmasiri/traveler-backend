package com.traveler.core.service.Impl;

import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.entity.provider.Item;
import com.traveler.common.utils.STATUS;
import com.traveler.core.repository.ItemRepository;
import com.traveler.core.service.ProviderItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Service
public class ProviderItemServiceImpl implements ProviderItemService {
    private ItemRepository itemRepository;

    public ProviderItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public ResponseEntity<String> createItem(ItemDTO itemDTO) {
        try {
            Item item = new Item();
            item.setName(itemDTO.getName());
            item.setCategory(itemDTO.getCategory());
            item.setContact(itemDTO.getContact());
            item.setDescription(itemDTO.getDescription());
            item.setPricePerDay(itemDTO.getPricePerDay());
            String imgList = String.join(",", itemDTO.getImages());
            item.setImages(imgList);
            item.setStatus(STATUS.INACTIVE);

            Item savedItem = itemRepository.save(item);
            return ResponseEntity.ok("DONE");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("ERROR");
        }
    }

    @Override
    public ResponseEntity<String> updateItem(ItemDTO itemDTO) {
        try {
            Item item = itemRepository.findById(itemDTO.getId()).orElseThrow(() -> new RuntimeException("Item not found"));
            item.setName(itemDTO.getName());
            item.setCategory(itemDTO.getCategory());
            item.setContact(itemDTO.getContact());
            item.setDescription(itemDTO.getDescription());
            item.setPricePerDay(itemDTO.getPricePerDay());
            String imgList = String.join(",", itemDTO.getImages());
            item.setImages(imgList);
            item.setStatus(STATUS.INACTIVE);

            Item savedItem = itemRepository.save(item);
            return ResponseEntity.ok("DONE");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("ERROR");
        }
    }

    @Override
    public ResponseEntity<String> deleteItem(Long itemId) {
        try {
            itemRepository.deleteById(itemId);
            return ResponseEntity.ok("DONE");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("ERROR");
        }
    }

    @Override
    public ResponseEntity<List<ItemDTO>> getItems() {
        try {
            List<Item> all = itemRepository.findAll();
            List<ItemDTO> itemDTOs = all.stream()
                    .map(this::convertToDTO)
                    .toList();
            return ResponseEntity.ok(itemDTOs);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @Override
    public ResponseEntity<ItemDTO> getItem(Long itemId) {
        Optional<Item> byId = itemRepository.findById(itemId);
        if(byId.isPresent()){
            ItemDTO dto = convertToDTO(byId.get());
            return ResponseEntity.ok(dto);
        }else{
            return ResponseEntity.ok(null);
        }
    }

    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setName(item.getName());
        dto.setId(item.getId());
        dto.setCategory(item.getCategory());
        dto.setContact(item.getContact());
        dto.setDescription(item.getDescription());
        dto.setPricePerDay(item.getPricePerDay());
        dto.setStatus(item.getStatus());
        dto.setImages(List.of(item.getImages().split(",")));
        return dto;
    }
}
