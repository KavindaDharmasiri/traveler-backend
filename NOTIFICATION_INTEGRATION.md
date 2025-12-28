# Notification Service Integration Guide

## Backend Integration

### Send Notification from Auth Service
```java
@Autowired
private NotificationClient notificationClient;

// Example: New user registration
Map<String, String> request = Map.of(
    "senderTenant", "system",
    "receiverTenant", user.getEmail(),
    "title", "Welcome to Traveler!",
    "message", "Your account has been created successfully",
    "notificationType", "REGISTRATION",
    "referenceId", user.getId().toString()
);
notificationClient.sendNotification(request);
```

### Send Notification from Core Service
```java
@Autowired
private NotificationClient notificationClient;

// Example: New order notification
Map<String, String> providerNotif = Map.of(
    "senderTenant", order.getClientTenant(),
    "receiverTenant", order.getProviderTenant(),
    "title", "New Order Received",
    "message", "You have received a new order from " + order.getCustomerName(),
    "notificationType", "ORDER",
    "referenceId", order.getOrderCode()
);
notificationClient.sendNotification(providerNotif);

// Customer notification
Map<String, String> customerNotif = Map.of(
    "senderTenant", order.getProviderTenant(),
    "receiverTenant", order.getClientTenant(),
    "title", "Order Confirmed",
    "message", "Your order " + order.getOrderCode() + " has been confirmed",
    "notificationType", "ORDER",
    "referenceId", order.getOrderCode()
);
notificationClient.sendNotification(customerNotif);
```

## Notification Types (Enum)
```java
public enum NotificationType {
    ORDER,          // Order related
    PAYMENT,        // Payment notifications
    REVIEW,         // Review notifications
    REGISTRATION,   // User registration
    SYSTEM,         // System notifications
    SUCCESS,        // Success messages
    REMINDER,       // Reminders
    MAP,            // Location/map related
    INVOICE,        // Invoice notifications
    POLICY          // Policy updates
}
```

## Frontend Integration

### User UI (Already Integrated)
- Notifications auto-fetch every 30 seconds
- Uses user email as tenantId
- Green (#217964) color for unread notifications
- Automatically marks as read on click

### Provider UI (Already Integrated)
- Notifications auto-fetch every 30 seconds
- Uses provider email as tenantId
- Green (#217964) color scheme matching UI
- Mark individual or all as read

## Color Scheme (Matching UI)
- Primary: `#217964` (Green)
- Hover: `#175D4E` (Darker Green)
- Unread: Full opacity with green background
- Read: 80% opacity

## API Endpoints (via Gateway)
- POST `/notification/notifications` - Create notification
- GET `/notification/notifications/tenant/{tenantId}` - Get all
- GET `/notification/notifications/tenant/{tenantId}/unread` - Get unread
- GET `/notification/notifications/tenant/{tenantId}/unread-count` - Count
- PUT `/notification/notifications/{id}/read` - Mark as read
- PUT `/notification/notifications/tenant/{tenantId}/read-all` - Mark all read
