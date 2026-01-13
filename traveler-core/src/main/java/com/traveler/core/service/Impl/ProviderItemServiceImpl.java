package com.traveler.core.service.Impl;

import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.provider.ItemReviewDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.provider.VehicleDetailsDTO;
import com.traveler.common.dto.provider.HotelDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import com.traveler.common.entity.provider.Item;
import com.traveler.common.entity.provider.VehicleDetails;
import com.traveler.common.entity.provider.HotelDetails;
import com.traveler.common.utils.STATUS;
import com.traveler.core.config.TenantContext;
import com.traveler.core.repository.ItemRepository;
import com.traveler.core.service.ProviderItemService;
import com.traveler.core.service.feign.AuthClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private com.traveler.core.service.feign.AuthClient authClient;

    public ProviderItemServiceImpl(ItemRepository itemRepository, com.traveler.core.service.feign.AuthClient authClient) {
        this.itemRepository = itemRepository;
        this.authClient = authClient;
    }

    @Override
    public ResponseEntity<String> createItem(ItemDTO itemDTO) {
        try {
            Item item = new Item();
            item.setName(itemDTO.getName() != null ? itemDTO.getName() : "Unknown");
            item.setCategory(itemDTO.getCategory() != null ? itemDTO.getCategory() : "GENERAL");
            item.setContact(itemDTO.getContact() != null ? itemDTO.getContact() : "N/A");
            item.setDescription(itemDTO.getDescription() != null ? itemDTO.getDescription() : "No description");
            item.setPricePerDay(itemDTO.getPricePerDay() > 0 ? itemDTO.getPricePerDay() : 1.0);
            item.setQty(itemDTO.getQty() > 0 ? itemDTO.getQty() : 1);
            String imgList = itemDTO.getImages() != null ? String.join(",", itemDTO.getImages()) : "";
            item.setImages(imgList);
            item.setStatus(STATUS.UNDER_REVIEW);
            item.setCurrency(itemDTO.getCurrency() != null ? itemDTO.getCurrency() : "USD");
            
            // Handle vehicle details for VEHICLE category
            if ("VEHICLES".equalsIgnoreCase(itemDTO.getCategory()) && itemDTO.getVehicleDetails() != null) {
                VehicleDetails vehicleDetails = new VehicleDetails();
                vehicleDetails.setVehicleNumber(itemDTO.getVehicleDetails().getVehicleNumber());
                vehicleDetails.setPassengerCount(itemDTO.getVehicleDetails().getPassengerCount());
                vehicleDetails.setCondition(itemDTO.getVehicleDetails().getCondition());
                vehicleDetails.setKmPerDay(itemDTO.getVehicleDetails().getKmPerDay());
                vehicleDetails.setPricePerExtraKm(itemDTO.getVehicleDetails().getPricePerExtraKm());
                vehicleDetails.setDriverStatus(itemDTO.getVehicleDetails().getDriverStatus());
                vehicleDetails.setWaitingChargePerNight(itemDTO.getVehicleDetails().getWaitingChargePerNight());
                item.setVehicleDetails(vehicleDetails);
            }
            
            // Handle hotel details for HOTEL category
            if ("HOTELS".equalsIgnoreCase(itemDTO.getCategory()) && itemDTO.getHotelDetails() != null) {
                HotelDetails hotelDetails = new HotelDetails();
                hotelDetails.setAddress(itemDTO.getHotelDetails().getAddress());
                hotelDetails.setRoomNumber(itemDTO.getHotelDetails().getRoomNumber());
                hotelDetails.setMaxGuests(itemDTO.getHotelDetails().getMaxGuests());
                item.setHotelDetails(hotelDetails);
            }

            Item savedItem = itemRepository.save(item);
            return ResponseEntity.ok("DONE");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("ERROR: " + e.getMessage());
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
    public ResponseEntity<Map<String, Object>> getItemsForTraveller(int page, int size, String category, String provider, Double minPrice, Double maxPrice, Double minRating) {
        try{
            return authClient.getAllForTraveller(page, size, category, provider, minPrice, maxPrice, minRating);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public ResponseEntity<Map<String, Object>> getFilters() {
        try{
            return authClient.getFilters();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ItemDetailsDTO> getItemsForTravellers() {
        try{
            List<Item> allByStatus = itemRepository.findAllByStatus(STATUS.ACTIVE);
            List<ItemDetailsDTO> itemDetailsList = allByStatus.stream()
                    .map(this::convertToDetailsDTO)
                    .toList();
            return itemDetailsList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseEntity<ItemDTO> getItemforTraveller(Long itemId, String tenant) {
        try {
            return authClient.getItemForTraveler(itemId,tenant);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @Override
    public ResponseEntity<List<ItemDTO>> getItemsWithStatus(String status) {
        try {
            List<Item> all = itemRepository.findAllByStatus(STATUS.valueOf(status));
            List<ItemDTO> itemDTOs = all.stream()
                    .map(this::convertToDTO)
                    .toList();
            System.out.println(itemDTOs.size());
            return ResponseEntity.ok(itemDTOs);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @Override
    public ResponseEntity<String> updateItemStatus(String status, long id) {
        try{
         itemRepository.findById(id).ifPresent(item -> {
             item.setStatus(STATUS.valueOf(status));
             itemRepository.save(item);
         });
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
            System.out.println(itemDTOs.size());
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
        dto.setCurrency(item.getCurrency());
        dto.setOverallRating(item.getOverallRating());
        dto.setTenant(TenantContext.getCurrentTenant());
        dto.setQty(item.getQty());
        dto.setImages(List.of(item.getImages().split(",")));
        
        // Convert vehicle details if present
        if (item.getVehicleDetails() != null) {
            VehicleDetailsDTO vehicleDTO = new VehicleDetailsDTO();
            vehicleDTO.setVehicleNumber(item.getVehicleDetails().getVehicleNumber());
            vehicleDTO.setPassengerCount(item.getVehicleDetails().getPassengerCount());
            vehicleDTO.setCondition(item.getVehicleDetails().getCondition());
            vehicleDTO.setKmPerDay(item.getVehicleDetails().getKmPerDay());
            vehicleDTO.setPricePerExtraKm(item.getVehicleDetails().getPricePerExtraKm());
            vehicleDTO.setDriverStatus(item.getVehicleDetails().getDriverStatus());
            vehicleDTO.setWaitingChargePerNight(item.getVehicleDetails().getWaitingChargePerNight());
            dto.setVehicleDetails(vehicleDTO);
        }
        
        // Convert hotel details if present
        if (item.getHotelDetails() != null) {
            HotelDetailsDTO hotelDTO = new HotelDetailsDTO();
            hotelDTO.setAddress(item.getHotelDetails().getAddress());
            hotelDTO.setRoomNumber(item.getHotelDetails().getRoomNumber());
            hotelDTO.setMaxGuests(item.getHotelDetails().getMaxGuests());
            dto.setHotelDetails(hotelDTO);
        }

        if (item.getReviews() != null) {
            List<ItemReviewDTO> itemReviewDTOS = new ArrayList<>();
            item.getReviews().forEach(review -> {
               ItemReviewDTO itemReviewDTO = new ItemReviewDTO();
               itemReviewDTO.setTravelerTenant(review.getTravelerTenant());
               itemReviewDTO.setReviewText(review.getReviewText());
               itemReviewDTO.setRating(review.getRating());
               itemReviewDTO.setCreatedAt(review.getCreatedAt());

               itemReviewDTOS.add(itemReviewDTO);
            });

            dto.setReviews(itemReviewDTOS);
        }
        
        return dto;
    }
    
    private ItemDetailsDTO convertToDetailsDTO(Item item) {
        ItemDetailsDTO dto = new ItemDetailsDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setCategory(item.getCategory());
        dto.setContact(item.getContact());
        dto.setDescription(item.getDescription());
        dto.setPricePerDay(item.getPricePerDay());
        dto.setOverallRating(item.getOverallRating());
        dto.setStatus(item.getStatus());
        dto.setCurrency(item.getCurrency());
        dto.setQty(item.getQty());
        dto.setImages(List.of(item.getImages().split(",")));
        
        if (item.getVehicleDetails() != null) {
            VehicleDetailsDTO vehicleDTO = new VehicleDetailsDTO();
            vehicleDTO.setVehicleNumber(item.getVehicleDetails().getVehicleNumber());
            vehicleDTO.setPassengerCount(item.getVehicleDetails().getPassengerCount());
            vehicleDTO.setCondition(item.getVehicleDetails().getCondition());
            vehicleDTO.setKmPerDay(item.getVehicleDetails().getKmPerDay());
            vehicleDTO.setPricePerExtraKm(item.getVehicleDetails().getPricePerExtraKm());
            vehicleDTO.setDriverStatus(item.getVehicleDetails().getDriverStatus());
            vehicleDTO.setWaitingChargePerNight(item.getVehicleDetails().getWaitingChargePerNight());
            dto.setVehicleDetails(vehicleDTO);
        }
        
        if (item.getHotelDetails() != null) {
            HotelDetailsDTO hotelDTO = new HotelDetailsDTO();
            hotelDTO.setAddress(item.getHotelDetails().getAddress());
            hotelDTO.setRoomNumber(item.getHotelDetails().getRoomNumber());
            hotelDTO.setMaxGuests(item.getHotelDetails().getMaxGuests());
            dto.setHotelDetails(hotelDTO);
        }
        
        return dto;
    }
}
