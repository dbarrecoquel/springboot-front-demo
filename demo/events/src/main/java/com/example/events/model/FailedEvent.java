package com.example.events.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "failed_events")
public class FailedEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String eventId;
    
    @Column(nullable = false)
    private String eventType; // "ProductViewEvent" ou "CategoryViewEvent"
    
    @Column(nullable = false)
    private String topic;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON de l'événement
    
    @Column(nullable = false)
    private String errorMessage;
    
    @Column(nullable = false)
    private Integer retryCount = 0;
    
    @Column(nullable = false)
    private Integer maxRetries = 3;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastRetryAt;
    
    @Column
    private LocalDateTime succeededAt;
    
    @Column(nullable = false)
    private String status; // "PENDING", "RETRYING", "SUCCESS", "FAILED"
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
    
    // Constructeurs
    public FailedEvent() {
    }
    
    public FailedEvent(String eventId, String eventType, String topic, String payload, String errorMessage) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.retryCount = 0;
        this.maxRetries = 3;
        this.status = "PENDING";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public String getPayload() {
        return payload;
    }
    
    public void setPayload(String payload) {
        this.payload = payload;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }
    
    public void setLastRetryAt(LocalDateTime lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }
    
    public LocalDateTime getSucceededAt() {
        return succeededAt;
    }
    
    public void setSucceededAt(LocalDateTime succeededAt) {
        this.succeededAt = succeededAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries && !"SUCCESS".equals(status);
    }
    
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
        this.status = "RETRYING";
    }
    
    public void markAsSuccess() {
        this.status = "SUCCESS";
        this.succeededAt = LocalDateTime.now();
    }
    
    public void markAsFailed() {
        this.status = "FAILED";
    }
}