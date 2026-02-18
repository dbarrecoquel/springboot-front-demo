package com.example.events.repository;

import com.example.events.model.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
    
    // Trouver les événements qui peuvent être retentés
    @Query("SELECT f FROM FailedEvent f WHERE f.status IN ('PENDING', 'RETRYING') " +
           "AND f.retryCount < f.maxRetries " +
           "AND (f.lastRetryAt IS NULL OR f.lastRetryAt < :retryAfter)")
    List<FailedEvent> findEventsToRetry(LocalDateTime retryAfter);
    
    // Statistiques
    long countByStatus(String status);
    
    List<FailedEvent> findByEventId(String eventId);
}