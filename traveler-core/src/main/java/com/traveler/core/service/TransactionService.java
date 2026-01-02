package com.traveler.core.service;

import com.traveler.common.dto.NotificationDTO;
import com.traveler.common.entity.*;
import com.traveler.common.entity.provider.Item;
import com.traveler.common.utils.STATUS;
import com.traveler.core.config.TenantContext;
import com.traveler.core.repository.*;
import com.traveler.core.service.feign.AuthClient;
import com.traveler.core.service.feign.NotificationClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CartService cartService;
    private final ItemRepository itemRepository;
    private final PastOrderRepository pastOrderRepository;
    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final PastOrderItemsRepository pastOrderItemsRepository;
    private final AuthClient authClient;
    private final NotificationClient notificationClient;

    public TransactionService(TransactionRepository transactionRepository, CartService cartService, ItemRepository itemRepository, PastOrderRepository pastOrderRepository, CartRepository cartRepository, CartItemsRepository cartItemsRepository, PastOrderItemsRepository pastOrderItemsRepository, AuthClient authClient, NotificationClient notificationClient) {
        this.transactionRepository = transactionRepository;
        this.cartService = cartService;
        this.itemRepository = itemRepository;
        this.pastOrderRepository = pastOrderRepository;
        this.cartRepository = cartRepository;
        this.cartItemsRepository = cartItemsRepository;
        this.pastOrderItemsRepository = pastOrderItemsRepository;
        this.authClient = authClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public List<Transaction> processPayment(Double subtotal, Double taxAmount, Double taxRate, Double totalAmount, String customerName, String customerTenant, List<String> orderCodes) {
        String tenantId = TenantContext.getCurrentTenant();
        List<Transaction> transactions = new ArrayList<>();
        List<Transaction> transactions1 = new ArrayList<>();
        List<Cart> cartsToDelete = new ArrayList<>();
        
        try {
            for (String orderCode : orderCodes) {
                Optional<Cart> cartOpt = cartRepository.findByOrderCode(orderCode);
                if (cartOpt.isPresent()) {
                    Cart cart = cartOpt.get();
                    List<CartItems> cartItems = cartItemsRepository.findByCart(cart);
                    
                    // Create separate transaction for each order
                    Transaction transaction = new Transaction();
                    transaction.setTransactionCode("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    transaction.setUserTenant(tenantId);
                    transaction.setCustomerName(customerName);
                    transaction.setCustomerTenant(customerTenant);
                    transaction.setOrderCodes(orderCode);
                    transaction.setSubtotal(cartItems.stream().mapToDouble(CartItems::getTotalPrice).sum());
                    transaction.setTaxAmount(taxAmount / orderCodes.size());
                    transaction.setTaxRate(taxRate);
                    transaction.setTotalAmount(cartItems.stream().mapToDouble(CartItems::getTotalPrice).sum() + (taxAmount / orderCodes.size()));
                    transaction.setPaymentMethod("CREDIT_CARD");
                    transaction.setPaymentStatus("COMPLETED");

                    // Create transaction items from cart items
                    List<TransactionItem> transactionItems = cartItems.stream()
                        .map(item -> {
                            TransactionItem txnItem = new TransactionItem();
                            txnItem.setTransaction(transaction);
                            txnItem.setCartId(cart.getId());
                            txnItem.setOrderCode(cart.getOrderCode());
                            txnItem.setItemId(item.getItem());
                            txnItem.setItemName("Item " + item.getItem());
                            txnItem.setProviderTenant(item.getProviderTenant());
                            txnItem.setQuantity(item.getQty());
                            txnItem.setRentalDays(item.getRentalDays());
                            txnItem.setPickupDate(item.getPickupDate());
                            txnItem.setReturnDate(item.getReturnDate());
                            txnItem.setItemPrice(item.getTotalPrice());
                            txnItem.setBagCode(item.getBagCode());
                            return txnItem;
                        })
                        .toList();

                    transaction.setTransactionItems(transactionItems);
                    transactions1.add(transaction);
                    transactions.add(transactionRepository.save(transaction));
                    
                    // Create past orders for each cart item
                    for (CartItems cartItem : cartItems) {
                        createPastOrder(cart, cartItem);
                    }
                    
                    cartsToDelete.add(cart);
                }
            }

            // Save to external service via Feign
            authClient.saveTran(transactions1);
            
            // If everything successful, delete carts and cart items
            for (Cart cart : cartsToDelete) {
                cartItemsRepository.deleteByCart(cart);
                cartRepository.delete(cart);
            }
            
            return transactions;
            
        } catch (Exception e) {
            // Transaction will automatically rollback due to @Transactional
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<String> createPastOrder(Cart mainOrderee, CartItems backpack) {
        try {
            PastOrder mainOrder = new PastOrder();
            mainOrder = mapPastOrder(mainOrderee);
            Optional<PastOrder> byOrderCode = pastOrderRepository.findByOrderCode(mainOrder.getOrderCode());
            if (byOrderCode.isPresent()) {
                mainOrder = byOrderCode.get();
            } else {
                mainOrder.setStatus(STATUS.PAYED);
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
            orderItem.setStatus(STATUS.PAYED);
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

    private PastOrder mapPastOrder(Cart mainOrderee) {
        PastOrder mainOrder = new PastOrder();
        mainOrder.setOrderCode(mainOrderee.getOrderCode());
        mainOrder.setCustomerName(mainOrderee.getCustomerName());
        mainOrder.setItem(mainOrderee.getItem());
        mainOrder.setStatus(mainOrderee.getStatus());
        mainOrder.setClientTenant(mainOrderee.getUserTenant());
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

    public List<Transaction> getTransactionHistory() {
        String tenantId = TenantContext.getCurrentTenant();
        return transactionRepository.findByUserTenantOrderByCreatedAtDesc(tenantId);
    }

    public void processPaymentProvider(Transaction transaction, TransactionItem item) {
        try{
            System.out.println("============================================================");
            System.out.println(item.getQuantity());
            System.out.println(item.getItemId());
            System.out.println(item.getBagCode());
            Optional<Item> byId = itemRepository.findById(Long.valueOf(item.getItemId()));
            System.out.println(byId.get().getQty());
            if (byId.get().getQty() >= item.getQuantity()) {
                byId.get().setQty(byId.get().getQty() - item.getQuantity());
                itemRepository.save(byId.get());
            } else {
                throw new RuntimeException("Insufficient item quantity for "+item.getItemName());
            }
            Transaction byTransactionCode = transactionRepository.findByTransactionCode(transaction.getTransactionCode());
            if (byTransactionCode == null) {
                Transaction transaction1 = new Transaction();
                transaction1.setTransactionCode(transaction.getTransactionCode());
                transaction1.setUserTenant(transaction.getUserTenant());
                transaction1.setCustomerName(transaction.getCustomerName());
                transaction1.setCustomerTenant(transaction.getCustomerTenant());
                transaction1.setOrderCodes("");
                transaction1.setSubtotal(0.0);
                transaction1.setTaxAmount(0.0);
                transaction1.setTaxRate(0.0);
                transaction1.setTotalAmount(0.0);
                transaction1.setPaymentMethod(transaction.getPaymentMethod());
                transaction1.setPaymentStatus(STATUS.PAYED.name());
                transaction1.setTransactionItems(List.of(item));
                transactionRepository.save(transaction1);
            }else{
                item.setTransaction(byTransactionCode);
                List<TransactionItem> transactionItems = byTransactionCode.getTransactionItems();
                transactionItems.add(item);
                byTransactionCode.setTransactionItems(transactionItems);
                transactionRepository.save(byTransactionCode);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
