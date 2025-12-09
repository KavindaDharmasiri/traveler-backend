package com.traveler.notification.repository;

import com.traveler.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByReceiverTenantOrderByCreatedAtDesc(String receiverTenant);
    
    List<Notification> findByReceiverTenantAndIsReadOrderByCreatedAtDesc(String receiverTenant, boolean isRead);
    
    long countByReceiverTenantAndIsRead(String receiverTenant, boolean isRead);
}
