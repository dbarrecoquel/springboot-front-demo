package com.example.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class BasketCreatedEvent {

    private String eventId;
    private Long basketId;
    private String userEmail;
    private LocalDateTime createdAt;

    public BasketCreatedEvent() {}

    public BasketCreatedEvent(Long basketId, String userEmail) {
        this.eventId = UUID.randomUUID().toString();
        this.basketId = basketId;
        this.userEmail = userEmail;
        this.createdAt = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getBasketId() { return basketId; }
    public void setBasketId(Long basketId) { this.basketId = basketId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "BasketCreatedEvent{eventId='" + eventId + "', basketId=" + basketId +
               ", userEmail='" + userEmail + "'}";
    }
}