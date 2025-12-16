package com.traveler.auth.traveler.service;

import com.traveler.auth.traveler.entity.User;
import com.traveler.auth.traveler.feignClient.CoreClient;
import com.traveler.auth.traveler.repository.OrderRepository;
import com.traveler.auth.traveler.repository.UserRepository;
import com.traveler.auth.traveler.utils.UserType;
import com.traveler.common.dto.BulkOrderStatusUpdateDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import com.traveler.common.entity.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Service
public class OrderService {
    private final CoreClient coreClient;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(CoreClient coreClient, OrderRepository orderRepository, UserRepository userRepository) {
        this.coreClient = coreClient;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<String> createOrder(OrderDTO orderDTO) {
        try {
            if (orderDTO.getOrderCode() == null || orderDTO.getOrderCode().isEmpty()) {
                orderDTO.setOrderCode(createCode());
            }
            coreClient.createOrder(orderDTO, orderDTO.getProviderTenant());
            coreClient.createOrder(orderDTO,orderDTO.getClientTenant());


        createOrderForAdmin(orderDTO);
        return ResponseEntity.ok("Order created successfully in both tenants");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create order in one or both tenants");
        }
    }

    private void createOrderForAdmin(OrderDTO orderDTO) {
        try {
            Optional<Order> existingOrder = orderRepository.findByOrderCode(orderDTO.getOrderCode());
            Order order;
            if (existingOrder.isPresent()) {
                order = existingOrder.get();
            } else {
                order = new Order();
            }

            order.setCustomerName(orderDTO.getCustomerName());
            order.setOrderCode(orderDTO.getOrderCode());
            order.setItem(orderDTO.getItem());
            if (orderDTO.getStatus() == null){
                order.setStatus(com.traveler.common.utils.STATUS.PENDING);
            }else {
                order.setStatus(orderDTO.getStatus());
            }

            order.setTotalPrice(orderDTO.getTotalPrice());
            order.setRentalDays(orderDTO.getRentalDays());
            order.setClientTenant(orderDTO.getClientTenant());
            order.setProviderTenant(orderDTO.getProviderTenant());
            orderRepository.save(order);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String createCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public ResponseEntity<List<OrderDTO>> findAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            List<OrderDTO> orderDTOs = orders.stream().map(order -> {
                OrderDTO dto = new OrderDTO();
                dto.setId(order.getId());
                dto.setOrderCode(order.getOrderCode());
                dto.setCustomerName(order.getCustomerName());
                dto.setItem(order.getItem());
                dto.setStatus(order.getStatus());
                dto.setTotalPrice(order.getTotalPrice());
                dto.setRentalDays(order.getRentalDays());
                dto.setClientTenant(order.getClientTenant());
                dto.setProviderTenant(order.getProviderTenant());
                return dto;
            }).toList();
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<OrderDTO> findOrder(Long orderId) {
        try {
            System.out.println("Finding order with ID: " + orderId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            System.out.println("Order found: " + orderOpt.isPresent());
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                OrderDTO dto = new OrderDTO();
                dto.setId(order.getId());
                dto.setOrderCode(order.getOrderCode());
                dto.setCustomerName(order.getCustomerName());
                dto.setItem(order.getItem());
                dto.setStatus(order.getStatus());
                dto.setTotalPrice(order.getTotalPrice());
                dto.setRentalDays(order.getRentalDays());
                dto.setClientTenant(order.getClientTenant());
                dto.setProviderTenant(order.getProviderTenant());
                System.out.println("Returning order DTO: " + dto.getOrderCode());
                return ResponseEntity.ok(dto);
            } else {
                System.out.println("Order not found, returning 404");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("ERROR in findOrder: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<Map<String, Object>> getAllForTraveller(int page, int size) {
        try{
            List<User> allByTypeAndIsActive = userRepository.findAllByTypeAndIsActive(UserType.SERVICE_PROVIDER, true);
            List<ProviderItemGroupDTO> allProviders = new ArrayList<>();
            
            // Collect all items from all providers
            List<ItemDetailsDTO> allItems = new ArrayList<>();
            for (User user : allByTypeAndIsActive) {
                List<ItemDetailsDTO> response = coreClient.getItemsForTraveller(user.getTenantId());
                
                // Add provider info to each item
                for (ItemDetailsDTO item : response) {
                    item.setProviderName(user.getName());
                    item.setTenant(user.getTenantId());
                }
                allItems.addAll(response);
                
                ProviderItemGroupDTO group = new ProviderItemGroupDTO(
                        user.getTenantId(),
                        user.getName(),
                        response
                );
                allProviders.add(group);
            }
            
            // Apply pagination to flattened items
            int totalItems = allItems.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalItems);
            
            List<ItemDetailsDTO> paginatedItems = startIndex < totalItems ? 
                allItems.subList(startIndex, endIndex) : new ArrayList<>();
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", paginatedItems);
            response.put("providers", allProviders);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            response.put("totalItems", totalItems);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public ResponseEntity<ItemDTO> getItemForTraveler(Long itemId, String tenant) {
        try {
            return coreClient.getItem(itemId, tenant);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<String> changeStatus(BulkOrderStatusUpdateDTO bulkOrderStatusUpdateDTO) {
        try{
            bulkOrderStatusUpdateDTO.getOrderCodes().stream().forEach(orderCode -> {
                Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    coreClient.updateStatus(order.getOrderCode(), bulkOrderStatusUpdateDTO.getStatus().name(), order.getClientTenant());
                    coreClient.updateStatus(order.getOrderCode(), bulkOrderStatusUpdateDTO.getStatus().name(), order.getProviderTenant());
                    order.setStatus(bulkOrderStatusUpdateDTO.getStatus());
                    orderRepository.save(order);
                }
            });
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
