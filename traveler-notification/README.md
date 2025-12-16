# Traveler Notification Service

Multi-tenant notification service that stores notifications in the `traveler_auth` database.

## Features
- Multi-tenant notification support
- Sender and receiver tenant tracking
- Unread notification count
- Mark as read functionality
- Feign client integration

## Database
Uses `traveler_auth` database with `notifications` table (auto-created)

## Port
8085

## API Endpoints

### Create Notification (via Feign)
```
POST /notifications
{
  "senderTenant": "tenant1",
  "receiverTenant": "tenant2",
  "title": "New Order",
  "message": "You have a new order",
  "notificationType": "ORDER",
  "referenceId": "ORDER123"
}
```

### Get Notifications by Tenant
```
GET /notifications/tenant/{tenantId}
```

### Get Unread Notifications
```
GET /notifications/tenant/{tenantId}/unread
```

### Get Unread Count
```
GET /notifications/tenant/{tenantId}/unread-count
```

### Mark as Read
```
PUT /notifications/{notificationId}/read
```

### Mark All as Read
```
PUT /notifications/tenant/{tenantId}/read-all
```

## Usage from Other Services

```java
@Autowired
private NotificationClient notificationClient;

Map<String, String> request = new HashMap<>();
request.put("senderTenant", "provider123");
request.put("receiverTenant", "customer456");
request.put("title", "Order Confirmed");
request.put("message", "Your order has been confirmed");
request.put("notificationType", "ORDER");
request.put("referenceId", "ORD-001");

notificationClient.sendNotification(request);
```
