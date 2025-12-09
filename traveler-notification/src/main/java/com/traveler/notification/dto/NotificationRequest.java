package com.traveler.notification.dto;

import com.traveler.notification.entity.NotificationType;
import lombok.Data;

@Data
public class NotificationRequest {
    private String senderTenant;
    private String receiverTenant;
    private String title;
    private String message;
    private String notificationType;
    private String referenceId;
}
