# Notification Flow Documentation

## Order Flow Notifications

### 1. ORDER_REQUEST
**Trigger:** User sends order request
**Receiver:** Provider
**Timeout:** 1 hour (daytime 6AM-10PM), 3 hours (nighttime 10PM-6AM)
```java
Map<String, String> notification = Map.of(
    "senderTenant", userEmail,
    "receiverTenant", providerEmail,
    "title", "New Order Request",
    "message", "You have a new order request from " + userName,
    "notificationType", "ORDER_REQUEST",
    "referenceId", orderId
);
```

### 2. ORDER_ACCEPTED
**Trigger:** Provider accepts order
**Receiver:** User
```java
Map<String, String> notification = Map.of(
    "senderTenant", providerEmail,
    "receiverTenant", userEmail,
    "title", "Order Accepted",
    "message", "Your order has been accepted. Please proceed with payment.",
    "notificationType", "ORDER_ACCEPTED",
    "referenceId", orderId
);
```

### 3. ORDER_REJECTED
**Trigger:** Provider rejects order OR timeout expires
**Receiver:** User
```java
Map<String, String> notification = Map.of(
    "senderTenant", providerEmail,
    "receiverTenant", userEmail,
    "title", "Order Rejected",
    "message", "Your order request was not accepted.",
    "notificationType", "ORDER_REJECTED",
    "referenceId", orderId
);
```

### 4. PENDING_PAYMENT
**Trigger:** Order accepted, waiting for payment
**Receiver:** User
```java
Map<String, String> notification = Map.of(
    "senderTenant", "system",
    "receiverTenant", userEmail,
    "title", "Payment Pending",
    "message", "Please complete payment for order " + orderCode,
    "notificationType", "PENDING_PAYMENT",
    "referenceId", orderId
);
```

### 5. PAYMENT_RECEIVED
**Trigger:** User completes payment
**Receiver:** Provider
**Include:** Payment details (time, amount, transaction ID)
```java
Map<String, String> notification = Map.of(
    "senderTenant", userEmail,
    "receiverTenant", providerEmail,
    "title", "Payment Received",
    "message", "Payment of $" + amount + " received for order " + orderCode,
    "notificationType", "PAYMENT_RECEIVED",
    "referenceId", orderId
);
```

### 6. ORDER_CONFIRMED
**Trigger:** After payment verification
**Receiver:** User
```java
Map<String, String> notification = Map.of(
    "senderTenant", providerEmail,
    "receiverTenant", userEmail,
    "title", "Order Confirmed",
    "message", "Your order " + orderCode + " is confirmed!",
    "notificationType", "ORDER_CONFIRMED",
    "referenceId", orderId
);
```

### 7. ORDER_PLACED
**Trigger:** Order successfully placed
**Receiver:** Both User & Provider
**Action:** Send WhatsApp to provider, show provider details to user
```java
// To User
Map<String, String> userNotif = Map.of(
    "senderTenant", "system",
    "receiverTenant", userEmail,
    "title", "Order Placed Successfully",
    "message", "Provider contact: " + providerPhone + ", Location: " + providerLocation,
    "notificationType", "ORDER_PLACED",
    "referenceId", orderId
);

// To Provider (+ WhatsApp)
Map<String, String> providerNotif = Map.of(
    "senderTenant", "system",
    "receiverTenant", providerEmail,
    "title", "New Order Confirmed",
    "message", "Order " + orderCode + " is ready. Customer: " + userName,
    "notificationType", "ORDER_PLACED",
    "referenceId", orderId
);
```

### 8. ORDER_REMINDER
**Trigger:** 1 day before order start date
**Receiver:** Both User & Provider
```java
Map<String, String> notification = Map.of(
    "senderTenant", "system",
    "receiverTenant", email,
    "title", "Order Reminder",
    "message", "Your order starts tomorrow!",
    "notificationType", "ORDER_REMINDER",
    "referenceId", orderId
);
```

## Provider Notifications

### PROFILE_UPDATED
**Trigger:** Provider updates profile
```java
Map<String, String> notification = Map.of(
    "senderTenant", "system",
    "receiverTenant", providerEmail,
    "title", "Profile Updated",
    "message", "Your profile has been updated successfully",
    "notificationType", "PROFILE_UPDATED",
    "referenceId", userId
);
```

### ITEM_UPDATED
**Trigger:** Provider updates item details
```java
Map<String, String> notification = Map.of(
    "senderTenant", "system",
    "receiverTenant", providerEmail,
    "title", "Item Updated",
    "message", "Item " + itemName + " has been updated",
    "notificationType", "ITEM_UPDATED",
    "referenceId", itemId
);
```

### ANNUAL_REPORT
**Trigger:** Yearly report generation
```java
Map<String, String> notification = Map.of(
    "senderTenant", "system",
    "receiverTenant", providerEmail,
    "title", "Annual Report Available",
    "message", "Your annual report for " + year + " is ready",
    "notificationType", "ANNUAL_REPORT",
    "referenceId", reportId
);
```

### SYSTEM_UPGRADE
**Trigger:** System maintenance/upgrade
```java
Map<String, String> notification = Map.of(
    "senderTenant", "system",
    "receiverTenant", email,
    "title", "System Upgrade",
    "message", "System will be under maintenance on " + date,
    "notificationType", "SYSTEM_UPGRADE",
    "referenceId", null
);
```

## Timeout Logic

### Order Request Timeout
```java
LocalTime now = LocalTime.now();
boolean isDaytime = now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(22, 0));
int timeoutHours = isDaytime ? 1 : 3;

// Schedule timeout job
scheduler.schedule(() -> {
    if (order.getStatus() == STATUS.PENDING) {
        // Auto-reject and notify user
        order.setStatus(STATUS.CANCELLED);
        orderRepository.save(order);
        
        notificationClient.sendNotification(Map.of(
            "senderTenant", "system",
            "receiverTenant", userEmail,
            "title", "Order Request Expired",
            "message", "Your order request was not accepted within the time limit",
            "notificationType", "ORDER_REJECTED",
            "referenceId", orderId
        ));
    }
}, timeoutHours, TimeUnit.HOURS);
```

## Navigation Mapping (Frontend)

```javascript
const notificationNavigation = {
    ORDER_REQUEST: '/orders',           // Provider: Received Orders
    ORDER_ACCEPTED: '/my-bookings',     // User: My Bookings
    ORDER_REJECTED: '/my-bookings',     // User: My Bookings
    PENDING_PAYMENT: '/my-bookings',    // User: My Bookings
    PAYMENT_RECEIVED: '/orders',        // Provider: Received Orders
    ORDER_CONFIRMED: '/my-bookings',    // User: My Bookings
    ORDER_PLACED: '/my-bookings',       // User: My Bookings
    ORDER_REMINDER: '/my-bookings',     // Both: Bookings/Orders
    PROFILE_UPDATED: '/profile',        // Provider: Profile
    ITEM_UPDATED: '/services',          // Provider: My Services
    ANNUAL_REPORT: '/dashboard',        // Provider: Dashboard
    SYSTEM_UPGRADE: '/'                 // Both: Home
};
```
