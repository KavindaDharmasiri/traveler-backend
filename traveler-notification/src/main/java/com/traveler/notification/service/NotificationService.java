package com.traveler.notification.service;

import com.traveler.notification.dto.NotificationRequest;
import com.traveler.notification.dto.NotificationResponse;
import com.traveler.notification.entity.Notification;
import com.traveler.notification.entity.NotificationType;
import com.traveler.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setSenderTenant(request.getSenderTenant());
        notification.setReceiverTenant(request.getReceiverTenant());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        
        try {
            notification.setNotificationType(NotificationType.valueOf(request.getNotificationType()));
        } catch (Exception e) {
            notification.setNotificationType(NotificationType.SYSTEM);
        }
        
        notification.setReferenceId(request.getReferenceId());
        
        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    public List<NotificationResponse> getNotificationsByTenant(String tenantId) {
        return notificationRepository.findByReceiverTenantOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotificationsByTenant(String tenantId) {
        return notificationRepository.findByReceiverTenantAndIsReadOrderByCreatedAtDesc(tenantId, false)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String tenantId) {
        return notificationRepository.countByReceiverTenantAndIsRead(tenantId, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(String tenantId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByReceiverTenantAndIsReadOrderByCreatedAtDesc(tenantId, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setSenderTenant(notification.getSenderTenant());
        response.setReceiverTenant(notification.getReceiverTenant());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setNotificationType(notification.getNotificationType());
        response.setReferenceId(notification.getReferenceId());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
