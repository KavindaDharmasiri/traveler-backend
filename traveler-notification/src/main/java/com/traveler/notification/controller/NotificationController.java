package com.traveler.notification.controller;

import com.traveler.notification.dto.NotificationRequest;
import com.traveler.notification.dto.NotificationResponse;
import com.traveler.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(request));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(notificationService.getNotificationsByTenant(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(@PathVariable String tenantId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByTenant(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String tenantId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(tenantId)));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tenant/{tenantId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String tenantId) {
        notificationService.markAllAsRead(tenantId);
        return ResponseEntity.ok().build();
    }
}
