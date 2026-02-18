package com.example.events.service;

import com.example.events.model.FailedEvent;
import com.example.events.repository.FailedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FailedEventService {
    
    private static final Logger logger = LoggerFactory.getLogger(FailedEventService.class);
    
    private final FailedEventRepository failedEventRepository;
    private final ObjectMapper objectMapper;
    
    public FailedEventService(FailedEventRepository failedEventRepository, ObjectMapper objectMapper) {
        this.failedEventRepository = failedEventRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Enregistrer un événement échoué
     */
    @Transactional
    public void saveFailedEvent(String eventId, String eventType, String topic, Object event, String errorMessage) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            
            FailedEvent failedEvent = new FailedEvent(eventId, eventType, topic, payload, errorMessage);
            failedEventRepository.save(failedEvent);
            
            logger.warn("💾 Saved failed event: eventId={}, type={}, error={}", 
                eventId, eventType, errorMessage);
        } catch (Exception e) {
            logger.error("❌ Failed to save failed event: eventId={}", eventId, e);
        }
    }
    
    /**
     * Récupérer les événements à retenter
     */
    public List<FailedEvent> getEventsToRetry(int delayMinutes) {
        LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(delayMinutes);
        return failedEventRepository.findEventsToRetry(retryAfter);
    }
    
    /**
     * Marquer un événement comme réussi
     */
    @Transactional
    public void markAsSuccess(FailedEvent event) {
        event.markAsSuccess();
        failedEventRepository.save(event);
        logger.info("✅ Marked event as success: eventId={}", event.getEventId());
    }
    
    /**
     * Incrémenter le compteur de retry
     */
    @Transactional
    public void incrementRetry(FailedEvent event, String errorMessage) {
        event.incrementRetry();
        event.setErrorMessage(errorMessage);
        
        if (!event.canRetry()) {
            event.markAsFailed();
            logger.error("❌ Event max retries reached: eventId={}", event.getEventId());
        }
        
        failedEventRepository.save(event);
    }
    
    /**
     * Obtenir les statistiques
     */
    public void logStatistics() {
        long pending = failedEventRepository.countByStatus("PENDING");
        long retrying = failedEventRepository.countByStatus("RETRYING");
        long success = failedEventRepository.countByStatus("SUCCESS");
        long failed = failedEventRepository.countByStatus("FAILED");
        
        logger.info("📊 Failed Events Stats: PENDING={}, RETRYING={}, SUCCESS={}, FAILED={}", 
            pending, retrying, success, failed);
    }
}