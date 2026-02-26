package com.example.events.service;

import com.example.events.model.AddToBasketEvent;
import com.example.events.model.BasketViewEvent;
import com.example.events.model.CategoryViewEvent;
import com.example.events.model.FailedEvent;
import com.example.events.model.ProductViewEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventRetryScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(EventRetryScheduler.class);
    
    private final FailedEventService failedEventService;
    private final KafkaTemplate<String, ProductViewEvent> productKafkaTemplate;
    private final KafkaTemplate<String, CategoryViewEvent> categoryKafkaTemplate;
    private final KafkaTemplate<String, BasketViewEvent> basketKafkaTemplate;
    private final KafkaTemplate<String, AddToBasketEvent> addToBasketKafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public EventRetryScheduler(FailedEventService failedEventService,
                              KafkaTemplate<String, ProductViewEvent> productKafkaTemplate,
                              KafkaTemplate<String, CategoryViewEvent> categoryKafkaTemplate,
                              KafkaTemplate<String, BasketViewEvent> basketKafkaTemplate,
                              KafkaTemplate<String, AddToBasketEvent> addToBasketKafkaTemplate,
                              ObjectMapper objectMapper) {
        this.failedEventService = failedEventService;
        this.productKafkaTemplate = productKafkaTemplate;
        this.categoryKafkaTemplate = categoryKafkaTemplate;
        this.basketKafkaTemplate = basketKafkaTemplate;
        this.addToBasketKafkaTemplate = addToBasketKafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Tente de renvoyer les événements échoués toutes les 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void retryFailedEvents() {
        logger.info("🔄 Starting retry of failed events...");
        
        // Récupérer les événements à retenter (après 2 minutes d'attente)
        List<FailedEvent> eventsToRetry = failedEventService.getEventsToRetry(2);
        
        if (eventsToRetry.isEmpty()) {
            logger.info("✅ No failed events to retry");
            return;
        }
        
        logger.info("🔄 Found {} events to retry", eventsToRetry.size());
        
        for (FailedEvent failedEvent : eventsToRetry) {
            retryEvent(failedEvent);
        }
        
        // Afficher les statistiques
        failedEventService.logStatistics();
    }
    
    private void retryEvent(FailedEvent failedEvent) {
        try {
            logger.info("🔄 Retrying event: eventId={}, type={}, attempt={}/{}", 
                failedEvent.getEventId(), 
                failedEvent.getEventType(), 
                failedEvent.getRetryCount() + 1, 
                failedEvent.getMaxRetries());
            
            if ("ProductViewEvent".equals(failedEvent.getEventType())) {
                retryProductViewEvent(failedEvent);
            } else if ("CategoryViewEvent".equals(failedEvent.getEventType())) {
                retryCategoryViewEvent(failedEvent);
            } else if ("BasketViewEvent".equals(failedEvent.getEventType())) {
                retryBasketViewEvent(failedEvent);
            }
            else if ("AddToBasketEvent".equals(failedEvent.getEventType())) {
                retryAddToBasketEvent(failedEvent);
             
            } else {
                logger.error("❌ Unknown event type: {}", failedEvent.getEventType());
                failedEvent.markAsFailed();
            }
            
        } catch (Exception e) {
            logger.error("❌ Retry failed for event: eventId={}, error={}", 
                failedEvent.getEventId(), e.getMessage());
            
            failedEventService.incrementRetry(failedEvent, e.getMessage());
        }
    }
    
    private void retryProductViewEvent(FailedEvent failedEvent) throws Exception {
        ProductViewEvent event = objectMapper.readValue(failedEvent.getPayload(), ProductViewEvent.class);
        
        productKafkaTemplate.send(failedEvent.getTopic(), event.getProductId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("✅ Retry successful: eventId={}", failedEvent.getEventId());
                    failedEventService.markAsSuccess(failedEvent);
                } else {
                    logger.error("❌ Retry failed: eventId={}, error={}", 
                        failedEvent.getEventId(), ex.getMessage());
                    failedEventService.incrementRetry(failedEvent, ex.getMessage());
                }
            });
    }
    
    private void retryCategoryViewEvent(FailedEvent failedEvent) throws Exception {
        CategoryViewEvent event = objectMapper.readValue(failedEvent.getPayload(), CategoryViewEvent.class);
        
        categoryKafkaTemplate.send(failedEvent.getTopic(), event.getCategoryId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("✅ Retry successful: eventId={}", failedEvent.getEventId());
                    failedEventService.markAsSuccess(failedEvent);
                } else {
                    logger.error("❌ Retry failed: eventId={}, error={}", 
                        failedEvent.getEventId(), ex.getMessage());
                    failedEventService.incrementRetry(failedEvent, ex.getMessage());
                }
            });
    }
    
    private void retryBasketViewEvent(FailedEvent failedEvent) throws Exception {
        BasketViewEvent event = objectMapper.readValue(failedEvent.getPayload(), BasketViewEvent.class);
        
        basketKafkaTemplate.send(failedEvent.getTopic(), event.getBasketId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("✅ Retry successful: eventId={}", failedEvent.getEventId());
                    failedEventService.markAsSuccess(failedEvent);
                } else {
                    logger.error("❌ Retry failed: eventId={}, error={}", 
                        failedEvent.getEventId(), ex.getMessage());
                    failedEventService.incrementRetry(failedEvent, ex.getMessage());
                }
            });
    }
    private void retryAddToBasketEvent(FailedEvent failedEvent) throws Exception {
        AddToBasketEvent event = objectMapper.readValue(failedEvent.getPayload(), AddToBasketEvent.class);
        
        addToBasketKafkaTemplate.send(failedEvent.getTopic(), event.getBasketId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("✅ Retry successful: eventId={}", failedEvent.getEventId());
                    failedEventService.markAsSuccess(failedEvent);
                } else {
                    logger.error("❌ Retry failed: eventId={}, error={}", 
                        failedEvent.getEventId(), ex.getMessage());
                    failedEventService.incrementRetry(failedEvent, ex.getMessage());
                }
            });
    }
}