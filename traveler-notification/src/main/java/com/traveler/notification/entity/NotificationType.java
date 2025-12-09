package com.traveler.notification.entity;

public enum NotificationType {
    // Order flow
    ORDER_REQUEST,           // User sends order request to provider
    ORDER_ACCEPTED,          // Provider accepts order
    ORDER_REJECTED,          // Provider rejects order
    PENDING_PAYMENT,         // Waiting for user payment
    PAYMENT_RECEIVED,        // Payment received by provider
    ORDER_CONFIRMED,         // Order confirmed after payment
    ORDER_PLACED,            // Order successfully placed
    ORDER_REMINDER,          // 1 day before order start
    ORDER_COMPLETE,          // 1 day before order start

    // Payment
    PAYMENT_SUCCESS,         // Payment successful
    PAYMENT_FAILED,          // Payment failed
    
    // Profile & Items
    PROFILE_UPDATED,         // Profile details updated
    ITEM_UPDATED,            // Item details updated
    
    // System
    SYSTEM_UPGRADE,          // System upgrade notification
    ANNUAL_REPORT,           // Annual report
    
    // Legacy (keep for compatibility)
    ORDER,
    PAYMENT,
    REVIEW,
    REGISTRATION,
    SYSTEM,
    SUCCESS,
    REMINDER,
    MAP,
    INVOICE,
    POLICY
}
