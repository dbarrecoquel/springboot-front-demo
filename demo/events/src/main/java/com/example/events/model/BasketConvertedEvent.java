package com.example.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class BasketConvertedEvent {

    private String eventId;
    private Long basketId;
    private LocalDateTime convertedAt;

    public BasketConvertedEvent() {}

    public BasketConvertedEvent(Long basketId) {
        this.eventId = UUID.randomUUID().toString();
        this.basketId = basketId;
        this.convertedAt = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getBasketId() { return basketId; }
    public void setBasketId(Long basketId) { this.basketId = basketId; }
    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }

    @Override
    public String toString() {
        return "BasketConvertedEvent{eventId='" + eventId + "', basketId=" + basketId + "}";
    }
}