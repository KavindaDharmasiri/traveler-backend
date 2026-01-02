package com.traveler.auth.traveler.service;

import com.traveler.auth.traveler.entity.User;
import com.traveler.auth.traveler.feignClient.CoreClient;
import com.traveler.auth.traveler.repository.*;
import com.traveler.auth.traveler.utils.UserType;
import com.traveler.common.dto.AuthUpdateStatusDTO;
import com.traveler.common.dto.OrderDTO;
import com.traveler.common.dto.SeparateSaveOrderDTO;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.dto.provider.SaveTrandDTO;
import com.traveler.common.dto.traveller.ItemDetailsDTO;
import com.traveler.common.dto.traveller.ProviderItemGroupDTO;
import com.traveler.common.entity.*;
import com.traveler.common.utils.STATUS;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final UserRepository userRepository;

    public OrderService(CoreClient coreClient, OrderRepository orderRepository, TransactionRepository transactionRepository, TransactionItemRepository transactionItemRepository, OrderItemsRepository orderItemsRepository, UserRepository userRepository) {
        this.coreClient = coreClient;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.transactionItemRepository = transactionItemRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<String> createOrder(Order mainOrder, List<Backpack> backpacks) {
        try {
            backpacks.forEach(backpack -> {
                SeparateSaveOrderDTO saveOrderDTO = new SeparateSaveOrderDTO();
                saveOrderDTO.setOrder(mainOrder);
                saveOrderDTO.setBackpacks(backpack);
                coreClient.createOrder(saveOrderDTO, backpack.getProviderTenant());
                coreClient.createOrder(saveOrderDTO, mainOrder.getClientTenant());
            });


            createOrderForAdmin(mainOrder,backpacks);
            return ResponseEntity.ok("Order created successfully in both tenants");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create order in one or both tenants");
        }
    }

    private void createOrderForAdmin(Order orderDTO, List<Backpack> backpacks) {
        try {
            Optional<Order> existingOrder = orderRepository.findByOrderCode(orderDTO.getOrderCode());
            Order order;
            if (existingOrder.isPresent()) {
                order = existingOrder.get();
            } else {
                order = new Order();
                order.setCustomerName(orderDTO.getCustomerName());
                order.setOrderCode(orderDTO.getOrderCode());
                if (orderDTO.getStatus() == null){
                    order.setStatus(com.traveler.common.utils.STATUS.PENDING);
                }else {
                    order.setStatus(orderDTO.getStatus());
                }
                order.setClientTenant(orderDTO.getClientTenant());
                order = orderRepository.save(order);
            }

            for (Backpack backpack : backpacks) {
                OrderItems orderItem = new OrderItems();
                orderItem.setOrder(order);
                orderItem.setItem(Math.toIntExact(backpack.getItemId()));
                orderItem.setTotalPrice(backpack.getTotalPrice().doubleValue());
                orderItem.setRentalDays(backpack.getRentalDays());
                orderItem.setProviderTenant(backpack.getProviderTenant());
                orderItem.setPickupDate(backpack.getPickupDate());
                orderItem.setReturnDate(backpack.getReturnDate());
                orderItem.setBagCode(backpack.getCode());
                orderItem.setStatus(STATUS.PENDING);
                orderItem.setQty(backpack.getQty());
                orderItemsRepository.save(orderItem);
            }
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
                dto.setPickupDate(order.getPickupDate().toString());
                dto.setReturnDate(order.getReturnDate().toString());
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

    public ResponseEntity<Map<String, Object>> getAllForTraveller(int page, int size, String category, String provider, Double minPrice, Double maxPrice, Double minRating) {
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
            
            // Apply filters
            List<ItemDetailsDTO> filteredItems = allItems.stream()
                .filter(item -> category == null || category.equals(item.getCategory()))
                .filter(item -> provider == null || provider.equals(item.getProviderName()))
                .filter(item -> minPrice == null || item.getPricePerDay() >= minPrice)
                .filter(item -> maxPrice == null || item.getPricePerDay() <= maxPrice)
                .filter(item -> minRating == null || (item.getOverallRating() != null && item.getOverallRating() >= minRating))
                .toList();
            
            // Apply pagination to filtered items
            int totalItems = filteredItems.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalItems);
            
            List<ItemDetailsDTO> paginatedItems = startIndex < totalItems ? 
                filteredItems.subList(startIndex, endIndex) : new ArrayList<>();
            
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
    
    public ResponseEntity<Map<String, Object>> getFilters() {
        try{
            List<User> providers = userRepository.findAllByTypeAndIsActive(UserType.SERVICE_PROVIDER, true);
            List<String> providerNames = providers.stream().map(User::getName).distinct().sorted().toList();
            
            // Get categories from all providers
            Set<String> categoriesSet = new HashSet<>();
            for (User user : providers) {
                List<ItemDetailsDTO> items = coreClient.getItemsForTraveller(user.getTenantId());
                categoriesSet.addAll(items.stream().map(ItemDetailsDTO::getCategory).collect(Collectors.toSet()));
            }
            List<String> categories = categoriesSet.stream().sorted().toList();
            
            Map<String, Object> filters = new HashMap<>();
            filters.put("categories", categories);
            filters.put("providers", providerNames);
            
            return ResponseEntity.ok(filters);
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

    public ResponseEntity<String> changeStatus(String orderId, String itemId, String status, String tenant) {
        try{
            AuthUpdateStatusDTO map = new AuthUpdateStatusDTO();
            map.setOrderId(orderId);
            map.setItemId(itemId);
            map.setStatus(status);
            coreClient.updateOrderStatus(map, tenant);

            if (itemId != null) {
                Optional<OrderItems> orderItemOpt = orderItemsRepository.findByBagCode(itemId);
                if (orderItemOpt.isPresent()) {
                    OrderItems orderItem = orderItemOpt.get();
                    orderItem.setStatus(STATUS.valueOf(status));
                    orderItemsRepository.save(orderItem);
                }
                int i = orderItemsRepository.countByOrderAndStatusNot(orderItemOpt.get().getOrder(), STATUS.valueOf(status));
                if (i == 0){
                    Order orderItem = orderItemOpt.get().getOrder();
                    orderItem.setStatus(STATUS.valueOf(status));
                    orderRepository.save(orderItem);
                }
            } else {
                Optional<Order> orderOpt = orderRepository.findByOrderCode(orderId);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    order.setStatus(STATUS.valueOf(status));
                    orderRepository.save(order);
                }
                List<OrderItems> orderItems = orderItemsRepository.findByOrderId(orderOpt.get().getId());
                for (OrderItems orderItem : orderItems) {
                    orderItem.setStatus(STATUS.valueOf(status));
                    orderItemsRepository.save(orderItem);
                }
            }
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<String> changeStatusPAYED(String orderId, String itemId, String status, String tenant) {
        try{
            AuthUpdateStatusDTO map = new AuthUpdateStatusDTO();
            map.setOrderId(orderId);
            map.setItemId(itemId);
            map.setStatus(status);
            coreClient.updateOrderStatusPAYED(map, tenant);

            if (itemId != null) {
                Optional<OrderItems> orderItemOpt = orderItemsRepository.findByBagCode(itemId);
                if (orderItemOpt.isPresent()) {
                    OrderItems orderItem = orderItemOpt.get();
                    orderItem.setStatus(STATUS.valueOf(status));
                    orderItemsRepository.save(orderItem);
                }
                int i = orderItemsRepository.countByOrderAndStatusNot(orderItemOpt.get().getOrder(), STATUS.valueOf(status));
                if (i == 0){
                    Order orderItem = orderItemOpt.get().getOrder();
                    orderItem.setStatus(STATUS.valueOf(status));
                    orderRepository.save(orderItem);
                }
            } else {
                Optional<Order> orderOpt = orderRepository.findByOrderCode(orderId);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    order.setStatus(STATUS.valueOf(status));
                    orderRepository.save(order);
                }
                List<OrderItems> orderItems = orderItemsRepository.findByOrderId(orderOpt.get().getId());
                for (OrderItems orderItem : orderItems) {
                    orderItem.setStatus(STATUS.valueOf(status));
                    orderItemsRepository.save(orderItem);
                }
            }
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<String> saveTran(List<Transaction> transactions) {
        try {
            for (Transaction transaction : transactions) {
                Optional<Transaction> existingTran = transactionRepository.findByTransactionCode(transaction.getTransactionCode());
                
                if (existingTran.isPresent()) {
                    // Just process status updates for existing transaction items
                    for (TransactionItem item : transaction.getTransactionItems()) {
                        try {
                            changeStatusPAYED(null, item.getBagCode(), STATUS.PAYED.name(), item.getProviderTenant());
                        } catch (Exception e) {
                            System.err.println("Failed to update status for item: " + item.getBagCode());
                        }
                    }
                } else {
                    Transaction transaction1 = makeTranObhj(transaction);
                    // Save new transaction
//                    Transaction savedTransaction = transactionRepository.findByTransactionCode(transaction1.getTransactionCode()).get();
                    for (TransactionItem item : transaction.getTransactionItems()) {
                        try {
                            changeStatusPAYED(null, item.getBagCode(), STATUS.PAYED.name(), item.getProviderTenant());
                        } catch (Exception e) {
                            System.err.println("Failed to update status for item: " + item.getBagCode());
                        }
                    }
                }
            }
            
            try {
                saveTranForProvider(transactions);
            } catch (Exception e) {
                System.err.println("Failed to save provider transactions: " + e.getMessage());
            }
            
            return ResponseEntity.ok().body("save transactions");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to save transactions");
        }
    }

    private Transaction makeTranObhj(Transaction transaction) {
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionCode(transaction.getTransactionCode());
        transaction1.setUserTenant(transaction.getUserTenant());
        transaction1.setCustomerName(transaction.getCustomerName());
        transaction1.setCustomerTenant(transaction.getCustomerTenant());
        transaction1.setOrderCodes(transaction.getOrderCodes());
        transaction1.setSubtotal(transaction.getSubtotal());
        transaction1.setTaxAmount(transaction.getTaxAmount());
        transaction1.setTaxRate(transaction.getTaxRate());
        transaction1.setPaymentMethod(transaction.getPaymentMethod());
        transaction1.setPaymentStatus(transaction.getPaymentStatus());
        transaction1.setTotalAmount(transaction.getTotalAmount());
        transaction1.setVersion((long) 1.0);
        Transaction save = transactionRepository.save(transaction1);

        transaction.getTransactionItems().forEach(item -> {
            TransactionItem transactionItem = new TransactionItem();
            transactionItem.setTransaction(save);
            transactionItem.setOrderCode(item.getOrderCode());
            transactionItem.setCartId(0L);
            transactionItem.setItemId(item.getItemId());
            transactionItem.setItemName(item.getItemName());
            transactionItem.setProviderName(item.getProviderName());
            transactionItem.setBagCode(item.getBagCode());
            transactionItem.setProviderTenant(item.getProviderTenant());
            transactionItem.setQuantity(item.getQuantity());
            transactionItem.setRentalDays(item.getRentalDays());
            transactionItem.setPickupDate(item.getPickupDate());
            transactionItem.setReturnDate(item.getReturnDate());
            transactionItem.setItemPrice(item.getItemPrice());
            transactionItemRepository.save(transactionItem);
        });
        return save;
    }

    private void saveTranForProvider(List<Transaction> transactions) {
        try{
            for (Transaction transaction : transactions) {
                // Create new Transaction without ID
                Transaction newTransaction = new Transaction();
                newTransaction.setTransactionCode(transaction.getTransactionCode());
                newTransaction.setUserTenant(transaction.getUserTenant());
                newTransaction.setCustomerName(transaction.getCustomerName());
                newTransaction.setCustomerTenant(transaction.getCustomerTenant());
                newTransaction.setOrderCodes(transaction.getOrderCodes());
                newTransaction.setSubtotal(transaction.getSubtotal());
                newTransaction.setTaxAmount(transaction.getTaxAmount());
                newTransaction.setTaxRate(transaction.getTaxRate());
                newTransaction.setPaymentMethod(transaction.getPaymentMethod());
                newTransaction.setPaymentStatus(transaction.getPaymentStatus());
                newTransaction.setTotalAmount(transaction.getTotalAmount());
                
                for (TransactionItem item : transaction.getTransactionItems()) {
                    SaveTrandDTO saveTrandDTO = new SaveTrandDTO();
                    // Create new TransactionItem without ID
                    TransactionItem newItem = new TransactionItem();
                    newItem.setOrderCode(item.getOrderCode());
                    newItem.setCartId(item.getCartId());
                    newItem.setItemId(item.getItemId());
                    newItem.setItemName(item.getItemName());
                    newItem.setProviderName(item.getProviderName());
                    newItem.setBagCode(item.getBagCode());
                    newItem.setProviderTenant(item.getProviderTenant());
                    newItem.setQuantity(item.getQuantity());
                    newItem.setRentalDays(item.getRentalDays());
                    newItem.setPickupDate(item.getPickupDate());
                    newItem.setReturnDate(item.getReturnDate());
                    newItem.setItemPrice(item.getItemPrice());
                    
                    saveTrandDTO.setItem(newItem);
                    saveTrandDTO.setTransaction(newTransaction);
                    coreClient.saveTranItemForProvider(saveTrandDTO,item.getProviderTenant());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
