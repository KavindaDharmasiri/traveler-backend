package com.traveler.notification.dto;

import com.traveler.notification.entity.NotificationType;
import lombok.Data;
import java.util.Date;

@Data
public class NotificationResponse {
    private Long id;
    private String senderTenant;
    private String receiverTenant;
    private String title;
    private String message;
    private NotificationType notificationType;
    private String referenceId;
    private boolean isRead;
    private Date createdAt;
}
