package com.example.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class BasketAbandonedEvent {

    private String eventId;
    private Long basketId;
    private String userEmail;
    private LocalDateTime abandonedAt;

    public BasketAbandonedEvent() {}

    public BasketAbandonedEvent(Long basketId, String userEmail) {
        this.eventId = UUID.randomUUID().toString();
        this.basketId = basketId;
        this.userEmail = userEmail;
        this.abandonedAt = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getBasketId() { return basketId; }
    public void setBasketId(Long basketId) { this.basketId = basketId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public LocalDateTime getAbandonedAt() { return abandonedAt; }
    public void setAbandonedAt(LocalDateTime abandonedAt) { this.abandonedAt = abandonedAt; }

    @Override
    public String toString() {
        return "BasketAbandonedEvent{eventId='" + eventId + "', basketId=" + basketId +
               ", userEmail='" + userEmail + "'}";
    }
}