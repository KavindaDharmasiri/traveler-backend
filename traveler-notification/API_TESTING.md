# Notification Service API Testing

## Base URL
```
http://localhost:5555/notification
```

## 1. Create Notification (Order)
```bash
POST http://localhost:5555/notification/notifications
Content-Type: application/json

{
  "senderTenant": "provider@example.com",
  "receiverTenant": "customer@example.com",
  "title": "New Order Received",
  "message": "You have a new order #ORD-12345",
  "notificationType": "ORDER",
  "referenceId": "ORD-12345"
}
```

## 2. Create Notification (Payment)
```bash
POST http://localhost:5555/notification/notifications
Content-Type: application/json

{
  "senderTenant": "system",
  "receiverTenant": "customer@example.com",
  "title": "Payment Successful",
  "message": "Your payment of $150 has been processed",
  "notificationType": "PAYMENT",
  "referenceId": "PAY-67890"
}
```

## 3. Create Notification (Registration)
```bash
POST http://localhost:5555/notification/notifications
Content-Type: application/json

{
  "senderTenant": "system",
  "receiverTenant": "newuser@example.com",
  "title": "Welcome to Traveler!",
  "message": "Your account has been created successfully",
  "notificationType": "REGISTRATION",
  "referenceId": "USER-001"
}
```

## 4. Create Notification (Review)
```bash
POST http://localhost:5555/notification/notifications
Content-Type: application/json

{
  "senderTenant": "customer@example.com",
  "receiverTenant": "provider@example.com",
  "title": "New Review Received",
  "message": "You received a 5-star review!",
  "notificationType": "REVIEW",
  "referenceId": "REV-555"
}
```

## 5. Get All Notifications for Tenant
```bash
GET http://localhost:5555/notification/notifications/tenant/customer@example.com
```

## 6. Get Unread Notifications
```bash
GET http://localhost:5555/notification/notifications/tenant/customer@example.com/unread
```

## 7. Get Unread Count
```bash
GET http://localhost:5555/notification/notifications/tenant/customer@example.com/unread-count
```

## 8. Mark Single Notification as Read
```bash
PUT http://localhost:5555/notification/notifications/1/read
```

## 9. Mark All as Read for Tenant
```bash
PUT http://localhost:5555/notification/notifications/tenant/customer@example.com/read-all
```

## cURL Examples

### Create Order Notification
```bash
curl -X POST http://localhost:5555/notification/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "senderTenant": "provider@example.com",
    "receiverTenant": "customer@example.com",
    "title": "New Order Received",
    "message": "You have a new order #ORD-12345",
    "notificationType": "ORDER",
    "referenceId": "ORD-12345"
  }'
```

### Get Notifications
```bash
curl http://localhost:5555/notification/notifications/tenant/customer@example.com
```

### Get Unread Count
```bash
curl http://localhost:5555/notification/notifications/tenant/customer@example.com/unread-count
```

### Mark as Read
```bash
curl -X PUT http://localhost:5555/notification/notifications/1/read
```

## Postman Collection

### 1. Create Multiple Test Notifications
```json
// ORDER
{
  "senderTenant": "hotel@traveler.com",
  "receiverTenant": "john@example.com",
  "title": "Booking Confirmed",
  "message": "Your hotel booking for Grand Plaza has been confirmed",
  "notificationType": "ORDER",
  "referenceId": "BOOK-001"
}

// PAYMENT
{
  "senderTenant": "system",
  "receiverTenant": "john@example.com",
  "title": "Payment Received",
  "message": "Payment of $250 received successfully",
  "notificationType": "PAYMENT",
  "referenceId": "PAY-001"
}

// REMINDER
{
  "senderTenant": "system",
  "receiverTenant": "john@example.com",
  "title": "Upcoming Trip",
  "message": "Your trip starts in 2 days. Don't forget to pack!",
  "notificationType": "REMINDER",
  "referenceId": "TRIP-001"
}

// SUCCESS
{
  "senderTenant": "system",
  "receiverTenant": "john@example.com",
  "title": "Profile Updated",
  "message": "Your profile has been updated successfully",
  "notificationType": "SUCCESS",
  "referenceId": "PROFILE-001"
}
```

## Testing Flow
1. Start all services (config, discovery, gateway, auth, notification)
2. Create 3-4 notifications using POST endpoint
3. Get all notifications for tenant
4. Check unread count
5. Mark one as read
6. Check unread count again
7. Mark all as read
8. Verify all are read

## Expected Response Format
```json
{
  "id": 1,
  "senderTenant": "provider@example.com",
  "receiverTenant": "customer@example.com",
  "title": "New Order Received",
  "message": "You have a new order #ORD-12345",
  "notificationType": "ORDER",
  "referenceId": "ORD-12345",
  "isRead": false,
  "createdAt": "2025-01-15T10:30:00"
}
```
