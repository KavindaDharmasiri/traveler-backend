# Traveler Payment Service

A comprehensive payment microservice with PayHere integration for the Traveler application (Sri Lanka).

## Features

- **PayHere Integration**: Complete payment processing with PayHere
- **Payment Management**: Create and track payments
- **Refund Support**: Manual refund processing
- **Notification Handling**: Real-time payment status updates
- **Error Handling**: Comprehensive error handling and validation
- **Database Persistence**: Payment records with MySQL
- **Service Discovery**: Eureka client integration

## API Endpoints

### Payment Operations
- `POST /api/payments` - Create a new payment
- `GET /api/payments/{paymentId}` - Get payment details
- `GET /api/payments/user/{userId}` - Get user's payment history
- `POST /api/payments/refund` - Process a refund

### Webhooks
- `POST /api/webhooks/payhere` - PayHere notification endpoint

### Health Check
- `GET /api/payments/health` - Service health status

## Configuration

Update `application.yml` with your PayHere credentials:

```yaml
payhere:
  merchant:
    id: your_merchant_id_here
    secret: your_merchant_secret_here
  api:
    base-url: https://sandbox.payhere.lk
    checkout-url: https://sandbox.payhere.lk/pay/checkout
```

## Database Schema

The service creates a `payments` table with the following structure:
- Payment tracking with unique IDs
- PayHere integration fields
- Status management
- Audit timestamps

## Error Handling

- Custom `PaymentException` for business logic errors
- PayHere notification verification
- Validation error responses
- Global exception handler

## Security

- CORS configuration for cross-origin requests
- PayHere hash verification
- Input validation and sanitization

## Usage Example

```java
// Create payment request
PaymentRequest request = new PaymentRequest();
request.setUserId("user123");
request.setOrderId("order456");
request.setAmount(new BigDecimal("2500.00"));
request.setCurrency("LKR");
request.setCustomerEmail("customer@example.com");

// Call payment service
PaymentResponse response = paymentService.createPayment(request);
```

## Running the Service

1. Ensure MySQL is running
2. Update database credentials in `application.yml`
3. Set up Stripe credentials
4. Run: `mvn spring-boot:run`

The service will start on port 8086 and register with Eureka discovery service.