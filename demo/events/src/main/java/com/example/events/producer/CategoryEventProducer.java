package com.example.events.producer;

import com.example.events.model.CategoryViewEvent;
import com.example.events.service.FailedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CategoryEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryEventProducer.class);
    private static final String TOPIC = "category-view-events";
    
    private final KafkaTemplate<String, CategoryViewEvent> kafkaTemplate;
    private final FailedEventService failedEventService;
    
    public CategoryEventProducer(@Qualifier("categoryKafkaTemplate") KafkaTemplate<String, CategoryViewEvent> kafkaTemplate,
                                FailedEventService failedEventService) {
        this.kafkaTemplate = kafkaTemplate;
        this.failedEventService = failedEventService;
    }
    
    public void sendCategoryViewEvent(CategoryViewEvent event) {
        logger.info("📤 Sending CategoryViewEvent: eventId={}, categoryId={}, categoryName={}", 
            event.getEventId(), event.getCategoryId(), event.getCategoryName());
        
        CompletableFuture<SendResult<String, CategoryViewEvent>> future = 
            kafkaTemplate.send(TOPIC, event.getCategoryId().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // Succès - Metrics et monitoring
                logger.info("CategoryViewEvent sent successfully: eventId={}, offset={}, partition={}", 
                    event.getEventId(), 
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition());
                
                // TODO: Envoyer des métriques à Prometheus/Grafana
                // metricsService.incrementSuccessCounter("category-view-events");
                
            } else {
                // Échec - Sauvegarder pour retry
                logger.error("Failed to send CategoryViewEvent: eventId={}, error={}", 
                    event.getEventId(), ex.getMessage());
                
                // Sauvegarder l'événement échoué en base
                failedEventService.saveFailedEvent(
                    event.getEventId(),
                    "CategoryViewEvent",
                    TOPIC,
                    event,
                    ex.getMessage()
                );
                
                // TODO: Envoyer une alerte
                // alertService.sendAlert("Kafka producer failed", ex);
            }
        });
    }
}