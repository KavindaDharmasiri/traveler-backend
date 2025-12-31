package com.traveler.core.service.Impl;

import com.traveler.common.dto.*;
import com.traveler.common.dto.provider.ItemDTO;
import com.traveler.common.entity.*;
import com.traveler.common.utils.STATUS;
import com.traveler.common.utils.UserType;
import com.traveler.core.config.TenantContext;
import com.traveler.core.controller.CoreController;
import com.traveler.core.repository.*;
import com.traveler.common.entity.CartItems;
import com.traveler.core.service.OrderService;
import com.traveler.core.service.TenantOrderService;
import com.traveler.core.service.feign.AuthClient;
import com.traveler.core.service.feign.NotificationClient;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Service
public class OrderServiceImpl implements OrderService {

    private final TenantOrderService tenantOrderService;
    private final OrderRepository orderRepository;
    private final PastOrderRepository pastOrderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final PastOrderItemsRepository pastOrderItemsRepository;
    private final AuthClient authClient;
    private final CoreController coreController;
    private final NotificationClient notificationClient;
    private final CartRepository cartRepository;
    private final BackpackRepository backpackRepository;

    private final CartItemsRepository cartItemsRepository;

    public OrderServiceImpl(TenantOrderService tenantOrderService, OrderRepository orderRepository, PastOrderRepository pastOrderRepository, OrderItemsRepository orderItemsRepository, PastOrderItemsRepository pastOrderItemsRepository, AuthClient authClient, CoreController coreController, NotificationClient notificationClient, CartRepository cartRepository, BackpackRepository backpackRepository, CartItemsRepository cartItemsRepository) {
        this.tenantOrderService = tenantOrderService;
        this.orderRepository = orderRepository;
        this.pastOrderRepository = pastOrderRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.pastOrderItemsRepository = pastOrderItemsRepository;
        this.authClient = authClient;
        this.coreController = coreController;
        this.notificationClient = notificationClient;
        this.cartRepository = cartRepository;
        this.backpackRepository = backpackRepository;
        this.cartItemsRepository = cartItemsRepository;
    }

    @Override
    public ResponseEntity<String> createOrder(Order mainOrder, Backpack backpack) {
        try {
            Optional<Order> byOrderCode = orderRepository.findByOrderCode(mainOrder.getOrderCode());
            if (byOrderCode.isPresent()) {
                mainOrder = byOrderCode.get();
            } else {
                mainOrder = orderRepository.save(mainOrder);
            }

            // Create order items from backpack
//            for (Backpack backpack : backpacks) {
                OrderItems orderItem = new OrderItems();
                orderItem.setOrder(mainOrder);
                orderItem.setItem(Math.toIntExact(backpack.getItemId()));
                orderItem.setTotalPrice(backpack.getTotalPrice().doubleValue());
                orderItem.setRentalDays(backpack.getRentalDays());
                orderItem.setProviderTenant(backpack.getProviderTenant());
                orderItem.setPickupDate(backpack.getPickupDate());
                orderItem.setReturnDate(backpack.getReturnDate());
                orderItem.setBagCode(backpack.getCode());
                orderItem.setQty(backpack.getQty());
                orderItem.setStatus(STATUS.PENDING);
                orderItemsRepository.save(orderItem);
//            }

            // Send notification to provider
            try {
                NotificationDTO notification = new NotificationDTO();
                if (TenantContext.getCurrentTenant().equals(mainOrder.getClientTenant())) {
                    notification.setSenderTenant(orderItem.getProviderTenant());
                    notification.setReceiverTenant(mainOrder.getClientTenant());
                    notification.setTitle("New Order Request");
                    notification.setMessage("You have Created a new order request");
                } else {
                    notification.setSenderTenant(mainOrder.getClientTenant());
                    notification.setReceiverTenant(orderItem.getProviderTenant());
                    notification.setTitle("New Order Request");
                    notification.setMessage("You have a new order request from " + mainOrder.getCustomerName());
                }
                notification.setNotificationType("ORDER_REQUEST");
                notification.setReferenceId(mainOrder.getOrderCode());
                notificationClient.sendNotification(notification);
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }

            return ResponseEntity.ok("Order created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create order");
        }
    }

    @Override
    public ResponseEntity<String> createOrderWithSup(List<OrderDTO> orderDTOs) {
        try {
            List<Backpack> backpacks = backpackRepository.findAll();
            if (backpacks.isEmpty()) {
                return ResponseEntity.badRequest().body("No items in backpack");
            }

            // Create main order
            String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            Order mainOrder = new Order();
            mainOrder.setOrderCode(orderCode);
            mainOrder.setCustomerName(authClient.getUserByTenant(TenantContext.getCurrentTenant()).getName());
            mainOrder.setStatus(STATUS.PENDING);
            mainOrder.setClientTenant(TenantContext.getCurrentTenant());
//            mainOrder = orderRepository.save(mainOrder);

            // Create order items from backpack
//            for (Backpack backpack : backpacks) {
//                OrderItems orderItem = new OrderItems();
//                orderItem.setOrder(mainOrder);
//                orderItem.setItem(Math.toIntExact(backpack.getItemId()));
//                orderItem.setTotalPrice(backpack.getTotalPrice().doubleValue());
//                orderItem.setRentalDays(backpack.getRentalDays());
//                orderItem.setProviderTenant(backpack.getProviderTenant());
//                orderItem.setPickupDate(backpack.getPickupDate());
//                orderItem.setReturnDate(backpack.getReturnDate());
//                orderItem.setStatus(STATUS.PENDING);
//                orderItemsRepository.save(orderItem);
//            }
            SaveOrderDTO saveOrderDTO = new SaveOrderDTO();
            saveOrderDTO.setOrder(mainOrder);
            saveOrderDTO.setBackpacks(backpacks);
            authClient.saveOrder(saveOrderDTO);
            // Clear backpack after creating orders
            backpackRepository.deleteAll();

            return ResponseEntity.ok("All orders created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create orders");
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Map<String, Map<String, List<OrderDTO>>>> findAllOrders() {
        try {
            removeold();
            String currentTenant = TenantContext.getCurrentTenant();
            System.out.println("Current tenant: " + currentTenant);
            var currentUser = authClient.getUserByTenant(currentTenant);
            boolean isServiceProvider = "SERVICE_PROVIDER".equals(currentUser.getType());

            List<Order> orders = orderRepository.findAllByStatusNot(STATUS.DELETED);
            
            Map<String, Map<String, List<OrderDTO>>> groupedOrders = new HashMap<>();
            
            for (Order order : orders) {
                List<OrderItems> orderItems = orderItemsRepository.findByOrderId(order.getId());
                
                for (OrderItems orderItem : orderItems) {
                    if (!orderItem.getStatus().equals(STATUS.DELETED)) {
                        OrderDTO orderDTO = new OrderDTO();
                        orderDTO.setId(orderItem.getId());
                        orderDTO.setOrderCode(order.getOrderCode());
                        orderDTO.setCustomerName(order.getCustomerName());
                        ResponseEntity<ItemDTO> itemForTraveler = authClient.getItemForTraveler((long) orderItem.getItem(), orderItem.getProviderTenant());
                        orderDTO.setItemObj(itemForTraveler.getBody());
                        orderDTO.setItem(orderItem.getItem());
                        orderDTO.setStatus(orderItem.getStatus());
                        orderDTO.setTotalPrice(orderItem.getTotalPrice());
                        orderDTO.setRentalDays(orderItem.getRentalDays());
                        orderDTO.setClientTenant(order.getClientTenant());
                        orderDTO.setProviderTenant(orderItem.getProviderTenant());
                        orderDTO.setPickupDate(orderItem.getPickupDate().toString());
                        orderDTO.setReturnDate(orderItem.getReturnDate().toString());

                        String groupTenant = isServiceProvider ? order.getClientTenant() : orderItem.getProviderTenant();
                        var tenantUser = authClient.getUserByTenant(groupTenant);
                        String groupName = tenantUser.getName();

                        orderDTO.setGroupTenant(groupTenant);
                        orderDTO.setGroupName(groupName);

                        groupedOrders.computeIfAbsent(groupTenant, k -> new HashMap<>())
                                .computeIfAbsent(groupName, k -> new ArrayList<>())
                                .add(orderDTO);
                    }
                }
            }

            return ResponseEntity.ok(groupedOrders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<OrderDTO> findOrder(Long orderId) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setOrderCode(order.getOrderCode());
                orderDTO.setCustomerName(order.getCustomerName());
                orderDTO.setItem(order.getItem());
                orderDTO.setStatus(order.getStatus());
                ResponseEntity<ItemDTO> itemForTraveler = authClient.getItemForTraveler((long) order.getItem(), order.getProviderTenant());
                orderDTO.setItemObj(itemForTraveler.getBody());
                orderDTO.setTotalPrice(order.getTotalPrice());
                orderDTO.setRentalDays(order.getRentalDays());
                orderDTO.setClientTenant(order.getClientTenant());
                orderDTO.setProviderTenant(order.getProviderTenant());
                return ResponseEntity.ok(orderDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<List<OrderDTO>> findAllOrdersFromAdmin() {
        try {
            return authClient.getOrders();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<OrderDTO> getOrderFromAdmin(Long orderId) {
        try {
            return authClient.getOrder(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<String> updateStatusWithSup(BulkOrderStatusUpdateDTO updateDTO) {
        try {
//            return ResponseEntity.ok(authClient.updateOrderStatus(updateDTO));
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update orders");
        }
    }

    @Override
    public ResponseEntity<String> bulkUpdateOrderStatus(String orderCode, String status) {
        try {
            Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus(STATUS.valueOf(status));
                orderRepository.save(order);

                NotificationDTO notification = new NotificationDTO();

                if (authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType().name().equals("SERVICE_PROVIDER")) {
                    System.out.println("1");
                    notification.setSenderTenant(order.getProviderTenant());
                    notification.setReceiverTenant(order.getProviderTenant());
                } else {
                    System.out.println("2");
                    notification.setSenderTenant(order.getProviderTenant());
                    notification.setReceiverTenant(order.getClientTenant());

                    if (status.equals("ACCEPTED")) {
                        this.createCartForUser(orderCode);
                    }
                }
//                notification.setSenderTenant(TenantContext.getCurrentTenant());
//                notification.setReceiverTenant(order.getClientTenant());
                notification.setMessage("Your order status has been updated to " + status);
                if (status.equals("CANCELLED")) {
                    notification.setMessage("Your order has been cancelled");
                    notification.setNotificationType("ORDER_REJECTED");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                } else if (status.equals("COMPLETED")) {
                    notification.setMessage("Your order has been completed");
                    notification.setNotificationType("ORDER_COMPLETE");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                } else if (status.equals("ACCEPTED")) {
                    notification.setMessage("Your order has been completed");
                    notification.setNotificationType("ORDER_ACCEPTED");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                } else if (status.equals("PAYED")) {
                    notification.setMessage("Your order has been completed");
                    notification.setNotificationType("ORDER_PAYED");
                    notification.setTitle("Order "+status+" By "+order.getCustomerName());
                } else {
                    notification.setNotificationType("ORDER_UPDATE");
                    notification.setTitle("Your Order Is "+status+" By Provider");
                }
                notification.setReferenceId(order.getOrderCode());
                notificationClient.sendNotification(notification);

                return ResponseEntity.ok("Order updated successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update order");
        }
    }

    @Override
    public ResponseEntity<OrderWithItemsDTO> findOrderByCode(String orderCode) {
        try {
            Optional<Order> orderOptional = orderRepository.findByOrderCode(orderCode);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                List<OrderItems> orderItems = orderItemsRepository.findByOrderId(order.getId());
                
                OrderWithItemsDTO orderWithItems = new OrderWithItemsDTO();
                orderWithItems.setId(order.getId());
                orderWithItems.setOrderCode(order.getOrderCode());
                orderWithItems.setCustomerName(order.getCustomerName());
                orderWithItems.setStatus(order.getStatus().toString());
                orderWithItems.setClientTenant(order.getClientTenant());
                
                List<OrderDTO> items = new ArrayList<>();
                for (OrderItems orderItem : orderItems) {
                    OrderDTO itemDTO = new OrderDTO();
                    itemDTO.setId(orderItem.getId());
                    itemDTO.setItem(orderItem.getItem());
                    itemDTO.setTotalPrice(orderItem.getTotalPrice());
                    itemDTO.setRentalDays(orderItem.getRentalDays());
                    itemDTO.setProviderTenant(orderItem.getProviderTenant());
                    itemDTO.setPickupDate(orderItem.getPickupDate().toString());
                    itemDTO.setReturnDate(orderItem.getReturnDate().toString());
                    itemDTO.setStatus(orderItem.getStatus());
                    itemDTO.setOrderCode(order.getOrderCode());
                    itemDTO.setCustomerName(order.getCustomerName());
                    itemDTO.setClientTenant(order.getClientTenant());
                    itemDTO.setQty(orderItem.getQty());
                    UserResponse userByTenant = authClient.getUserByTenant(orderItem.getProviderTenant());
                    itemDTO.setProviderName(userByTenant.getName());
                    itemDTO.setMap(userByTenant.getGoogleMapsUrl());

                    ResponseEntity<ItemDTO> itemForTraveler = authClient.getItemForTraveler((long) orderItem.getItem(), orderItem.getProviderTenant());
                    itemDTO.setItemObj(itemForTraveler.getBody());
                    
                    items.add(itemDTO);
                }
                orderWithItems.setItems(items);
                
                return ResponseEntity.ok(orderWithItems);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Override
    public ResponseEntity<String> updateStatusSingle(Long orderId, Long itemId, String status) {
        try{
        String tennt = "";
        String tennt2 = "";
        String orderCode = "";
        String itemCode = "";
        if (itemId != null && itemId != 0){
            Optional<OrderItems> byId = orderItemsRepository.findById(itemId);
            byId.get().setStatus(STATUS.valueOf(status));
            tennt = byId.get().getOrder().getClientTenant();
            tennt2 = byId.get().getProviderTenant();
            itemCode = byId.get().getBagCode();
            Map map = new HashMap();
            map.put("orderId", orderCode);
            map.put("itemId", itemCode);
            map.put("status", status);
            map.put("tenant", tennt);
            authClient.updateOrderStatus(map);

            map.put("orderId", orderCode);
            map.put("itemId", itemCode);
            map.put("status", status);
            map.put("tenant", tennt2);
            authClient.updateOrderStatus(map);
//            orderItemsRepository.save(byId.get());
        }else {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus(STATUS.valueOf(status));
                tennt = order.getClientTenant();
                orderCode = order.getOrderCode();
//                orderRepository.save(order);

                orderItemsRepository.findByOrderId(order.getId()).forEach(oi -> {
                    oi.setStatus(STATUS.valueOf(status));
                    Map map = new HashMap();
                    map.put("orderId", orderOpt.get().getOrderCode());
                    map.put("itemId", oi.getBagCode());
                    map.put("status", status);
                    map.put("tenant", orderOpt.get().getClientTenant());
                    authClient.updateOrderStatus(map);

                    map.put("orderId", orderOpt.get().getOrderCode());
                    map.put("itemId", oi.getBagCode());
                    map.put("status", status);
                    map.put("tenant", oi.getProviderTenant());
                    authClient.updateOrderStatus(map);
                });
            }
        }

        return ResponseEntity.ok("Order updated successfully");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update order");
        }
    }

    @Override
    public ResponseEntity<String> authUpdatStatus(String orderId, String itemId, String status) {
//        removeold();
        try{
        if (itemId != null){
            Optional<OrderItems> byId = orderItemsRepository.findByBagCode(itemId);
            if (byId.isPresent()) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.out.println(authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType());
                System.out.println(authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType().equals(UserType.TRAVELLER));
                if (authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType().equals(UserType.TRAVELLER)) {
                    System.out.println("...........................................................");
                    if (status.equals("ACCEPTED")) {
                        System.out.println("[[[[[[[[[");
                        saveCart(orderId, itemId, status);
                    }if (status.equals("CANCELLED")) {
                        createPastOrder(byId.get().getOrder(), byId.get(),status);
                    }
                } else {
                    createPastOrder(byId.get().getOrder(), byId.get(),status);
                }
                byId.get().setStatus(STATUS.DELETED);
                orderItemsRepository.save(byId.get());

            }else{
                Optional<Order> orderOpt = orderRepository.findByOrderCode(orderId);
                if (orderOpt.isPresent()) {
                    System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[");
                    System.out.println(authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType());
                    if (authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType().equals(UserType.TRAVELLER)) {
                        if (status.equals("ACCEPTED")) {
                            saveCart(orderId, itemId, status);
                        }if (status.equals("CANCELLED")) {
                            createPastOrder(byId.get().getOrder(), byId.get(),status);
                        }
                    }else {
                        createPastOrder(byId.get().getOrder(), byId.get(),status);
                    }
                }
                orderOpt.get().setStatus(STATUS.DELETED);
                orderItemsRepository.findAllByOrder(orderOpt.get()).forEach(oi -> {
                    System.out.println(authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType());
                    if (authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType().equals(UserType.TRAVELLER)) {
                        if (status.equals("ACCEPTED")) {
                            saveCart(orderId, oi.getBagCode(), status);
                        }
                    }
                    try {
                        oi.setStatus(STATUS.DELETED);
                        orderItemsRepository.save(oi);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                orderItemsRepository.deleteByOrderId(orderOpt.get().getId());
                orderRepository.delete(orderOpt.get());
            }
        }else {
            Optional<Order> orderOpt = orderRepository.findByOrderCode(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                System.out.println(authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType());
                if (authClient.getUserByTenant(TenantContext.getCurrentTenant()).getType().equals(UserType.TRAVELLER)) {
                    if (status.equals("ACCEPTED")) {
                        saveCart(orderId, itemId, status);
                    }if (status.equals("CANCELLED")) {
                        orderItemsRepository.findByOrderId(order.getId()).forEach(oi -> {
                            createPastOrder(order, oi, status);
                        });
                    }
                } else {
                    orderItemsRepository.findByOrderId(order.getId()).forEach(oi -> {
                        createPastOrder(order, oi, status);
                    });
                }

//                orderItemsRepository.deleteByOrderId(order.getId());
                order.setStatus(STATUS.DELETED);
                orderRepository.save(order);
            }
        }

        return ResponseEntity.ok("Order updated successfully");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update order");
        }
    }

    private void removeold() {
        try {
            orderItemsRepository.deleteAllByStatus(STATUS.DELETED);
            orderRepository.deleteAllByStatus(STATUS.DELETED);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ResponseEntity<String> createPastOrder(Order mainOrderee, OrderItems backpack, String status) {
        try {
            PastOrder mainOrder = new PastOrder();
            mainOrder = mapPastOrder(mainOrderee);
            Optional<PastOrder> byOrderCode = pastOrderRepository.findByOrderCode(mainOrder.getOrderCode());
            if (byOrderCode.isPresent()) {
                mainOrder = byOrderCode.get();
            } else {
                mainOrder.setStatus(STATUS.valueOf(status));
                mainOrder = pastOrderRepository.save(mainOrder);
            }

            // Create order items from backpack
//            for (Backpack backpack : backpacks) {
            PastOrderItems orderItem = new PastOrderItems();
            orderItem.setOrder(mainOrder);
            orderItem.setItem(backpack.getItem());
            orderItem.setTotalPrice(backpack.getTotalPrice());
            orderItem.setRentalDays(backpack.getRentalDays());
            orderItem.setProviderTenant(backpack.getProviderTenant());
            orderItem.setPickupDate(backpack.getPickupDate());
            orderItem.setReturnDate(backpack.getReturnDate());
            orderItem.setBagCode(backpack.getBagCode());
            orderItem.setQty(backpack.getQty());
            orderItem.setStatus(STATUS.valueOf(status));
            pastOrderItemsRepository.save(orderItem);
//            }

            // Send notification to provider
            try {
                NotificationDTO notification = new NotificationDTO();
                if (TenantContext.getCurrentTenant().equals(mainOrder.getClientTenant())) {
                    notification.setSenderTenant(orderItem.getProviderTenant());
                    notification.setReceiverTenant(mainOrder.getClientTenant());
                    notification.setTitle("New Order Request");
                    notification.setMessage("You have Created a new order request");
                } else {
                    notification.setSenderTenant(mainOrder.getClientTenant());
                    notification.setReceiverTenant(orderItem.getProviderTenant());
                    notification.setTitle("New Order Request");
                    notification.setMessage("You have a new order request from " + mainOrder.getCustomerName());
                }
                notification.setNotificationType("ORDER_REQUEST");
                notification.setReferenceId(mainOrder.getOrderCode());
                notificationClient.sendNotification(notification);
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }

            return ResponseEntity.ok("Order created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create order");
        }
    }

    private PastOrder mapPastOrder(Order mainOrderee) {
        PastOrder mainOrder = new PastOrder();
        mainOrder.setOrderCode(mainOrderee.getOrderCode());
        mainOrder.setCustomerName(mainOrderee.getCustomerName());
        mainOrder.setItem(mainOrderee.getItem());
        mainOrder.setStatus(mainOrderee.getStatus());
        mainOrder.setClientTenant(mainOrderee.getClientTenant());
        mainOrder.setCreatedAt(mainOrderee.getCreatedAt());
        mainOrder.setUpdatedAt(mainOrderee.getUpdatedAt());
        mainOrder.setTotalPrice(mainOrderee.getTotalPrice());
        mainOrder.setRentalDays(mainOrderee.getRentalDays());
        mainOrder.setProviderTenant(mainOrderee.getProviderTenant());
        mainOrder.setPickupDate(mainOrderee.getPickupDate());
        mainOrder.setReturnDate(mainOrderee.getReturnDate());
        mainOrder.setQty(mainOrderee.getQty());
        return mainOrder;
    }

    private void saveCart(String orderId, String itemId, String status) {
        try{
            if (itemId != null){
                System.out.println(",,,,,,,,,,,,,,,,,,,,");
                Optional<OrderItems> orderItem = orderItemsRepository.findByBagCode(itemId);
                if (orderItem.isPresent()) {
                    System.out.println("ppppppppp");
                    String orderCode = orderItem.get().getOrder().getOrderCode();
                    
                    // Find or create cart
                    Optional<Cart> existingCart = cartRepository.findByOrderCode(orderCode);
                    Cart cart;
                    if (existingCart.isEmpty()) {
                        cart = new Cart();
                        cart.setUserTenant(TenantContext.getCurrentTenant());
                        cart.setOrderCode(orderCode);
                        cart = cartRepository.save(cart);
                    } else {
                        cart = existingCart.get();
                    }
                    
                    // Create cart item
                    CartItems cartItem = new CartItems();
                    cartItem.setCart(cart);
                    cartItem.setItem(orderItem.get().getItem());
                    cartItem.setQty(orderItem.get().getQty());
                    cartItem.setTotalPrice(orderItem.get().getTotalPrice());
                    cartItem.setRentalDays(orderItem.get().getRentalDays());
                    cartItem.setProviderTenant(orderItem.get().getProviderTenant());
                    cartItem.setPickupDate(orderItem.get().getPickupDate());
                    cartItem.setReturnDate(orderItem.get().getReturnDate());
                    cartItemsRepository.save(cartItem);
                }
            } else{
                Optional<Order> order = orderRepository.findByOrderCode(orderId);
                if (order.isPresent()) {
                    String orderCode = order.get().getOrderCode();
                    
                    // Find or create cart
                    Optional<Cart> existingCart = cartRepository.findByOrderCode(orderCode);
                    Cart cart;
                    if (existingCart.isEmpty()) {
                        cart = new Cart();
                        cart.setUserTenant(TenantContext.getCurrentTenant());
                        cart.setOrderCode(orderCode);
                        cart = cartRepository.save(cart);
                    } else {
                        cart = existingCart.get();
                    }
                    
                    // Create cart items for all order items
                    List<OrderItems> orderItems = orderItemsRepository.findByOrderId(order.get().getId());
                    for (OrderItems orderItem : orderItems) {
                        CartItems cartItem = new CartItems();
                        cartItem.setCart(cart);
                        cartItem.setItem(orderItem.getItem());
                        cartItem.setQty(orderItem.getQty());
                        cartItem.setTotalPrice(orderItem.getTotalPrice());
                        cartItem.setRentalDays(orderItem.getRentalDays());
                        cartItem.setProviderTenant(orderItem.getProviderTenant());
                        cartItem.setPickupDate(orderItem.getPickupDate());
                        cartItem.setReturnDate(orderItem.getReturnDate());
                        cartItemsRepository.save(cartItem);
                    }

                    orderItemsRepository.deleteByOrderId(order.get().getId());
                }

                orderRepository.deleteByOrderCode(orderId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void createCartForUser(String orderCode) {
        try {
            Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
            if (order != null) {
                Cart cart = new Cart();
                cart.setUserTenant(order.getClientTenant());
//                cart.setOrder(order);
                cartRepository.save(cart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void autoCancelPendingOrders() {
        try {
            List<OrderItems> pendingOrderItems = orderItemsRepository.findByStatus(STATUS.PENDING);
            Date now = new Date();
            
            for (OrderItems orderItem : pendingOrderItems) {
                if (orderItem.getCreatedAt() != null) {
                    long diffInMillis = now.getTime() - orderItem.getCreatedAt().getTime();
                    long diffInHours = diffInMillis / (1000 * 60 * 60);
                    
                    LocalTime currentTime = LocalTime.now();
                    boolean isDayTime = currentTime.isAfter(LocalTime.of(6, 0)) && currentTime.isBefore(LocalTime.of(18, 0));
                    
                    long timeoutHours = isDayTime ? 1 : 3;
                    
                    if (diffInHours >= timeoutHours) {
                        orderItem.setStatus(STATUS.CANCELLED);
                        orderItemsRepository.save(orderItem);
                        
                        // Check if all items in the order are now cancelled
                        Order order = orderItem.getOrder();
                        List<OrderItems> allOrderItems = orderItemsRepository.findByOrderId(order.getId());
                        boolean allCancelled = allOrderItems.stream().allMatch(item -> item.getStatus() == STATUS.CANCELLED);
                        
                        if (allCancelled) {
                            order.setStatus(STATUS.CANCELLED);
                            orderRepository.save(order);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in auto-cancel task: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<OrderWithItemsDTO>> findPastOrders() {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            List<PastOrder> pastOrders = pastOrderRepository.findAllByOrderByCreatedAtDesc();
            
            List<OrderWithItemsDTO> result = new ArrayList<>();
            
            for (PastOrder pastOrder : pastOrders) {
                OrderWithItemsDTO orderWithItems = new OrderWithItemsDTO();
                orderWithItems.setId(pastOrder.getId());
                orderWithItems.setOrderCode(pastOrder.getOrderCode());
                orderWithItems.setCustomerName(pastOrder.getCustomerName());
                orderWithItems.setStatus(pastOrder.getStatus().toString());
                orderWithItems.setClientTenant(pastOrder.getClientTenant());
                
                List<PastOrderItems> pastOrderItems = pastOrderItemsRepository.findByOrderId(pastOrder.getId());
                List<OrderDTO> items = new ArrayList<>();
                
                for (PastOrderItems pastOrderItem : pastOrderItems) {
                    OrderDTO itemDTO = new OrderDTO();
                    itemDTO.setId(pastOrderItem.getId());
                    itemDTO.setItem(pastOrderItem.getItem());
                    itemDTO.setTotalPrice(pastOrderItem.getTotalPrice());
                    itemDTO.setRentalDays(pastOrderItem.getRentalDays());
                    itemDTO.setProviderTenant(pastOrderItem.getProviderTenant());
                    itemDTO.setPickupDate(pastOrderItem.getPickupDate().toString());
                    itemDTO.setReturnDate(pastOrderItem.getReturnDate().toString());
                    itemDTO.setStatus(pastOrderItem.getStatus());
                    itemDTO.setOrderCode(pastOrder.getOrderCode());
                    itemDTO.setCustomerName(pastOrder.getCustomerName());
                    itemDTO.setClientTenant(pastOrder.getClientTenant());
                    itemDTO.setQty(pastOrderItem.getQty());
                    UserResponse userByTenant = authClient.getUserByTenant(pastOrderItem.getProviderTenant());
                    itemDTO.setProviderName(userByTenant.getName());
                    itemDTO.setMap(userByTenant.getGoogleMapsUrl());
                    itemDTO.setContact(userByTenant.getContactNumber());

                    try {
                        ResponseEntity<ItemDTO> itemResponse = authClient.getItemForTraveler((long) pastOrderItem.getItem(), pastOrderItem.getProviderTenant());
                        itemDTO.setItemObj(itemResponse.getBody());
                    } catch (Exception e) {
                        System.err.println("Error fetching item details: " + e.getMessage());
                    }
                    
                    items.add(itemDTO);
                }
                
                orderWithItems.setItems(items);
                result.add(orderWithItems);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

}
