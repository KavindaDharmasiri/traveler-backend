package com.traveler.common.dto;

public class NotificationDTO {
    private String senderTenant;
    private String receiverTenant;
    private String title;
    private String message;
    private String notificationType;
    private String referenceId;

    public String getSenderTenant() {
        return senderTenant;
    }

    public void setSenderTenant(String senderTenant) {
        this.senderTenant = senderTenant;
    }

    public String getReceiverTenant() {
        return receiverTenant;
    }

    public void setReceiverTenant(String receiverTenant) {
        this.receiverTenant = receiverTenant;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
