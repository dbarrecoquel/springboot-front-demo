package com.example.events.producer;

import com.example.events.model.CategoryViewEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryEventProducer.class);
    private static final String TOPIC = "category-view-events";
    
    private final KafkaTemplate<String, CategoryViewEvent> kafkaTemplate;
    
    public CategoryEventProducer(@Qualifier("categoryKafkaTemplate") KafkaTemplate<String, CategoryViewEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendCategoryViewEvent(CategoryViewEvent event) {
        logger.info("Sending CategoryViewEvent: eventId={}, categoryId={}, categoryName={}", 
            event.getEventId(), event.getCategoryId(), event.getCategoryName());
        
        try {
            CompletableFuture<SendResult<String, CategoryViewEvent>> future = 
                kafkaTemplate.send(TOPIC, event.getCategoryId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("CategoryViewEvent sent successfully: eventId={}, offset={}, partition={}", 
                        event.getEventId(), 
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
                } else {
                    logger.error("Failed to send CategoryViewEvent: eventId={}, error={}", 
                        event.getEventId(), ex.getMessage(), ex);
                }
            });
            
            future.get(5, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            logger.error("Error sending CategoryViewEvent: eventId={}, error={}", 
                event.getEventId(), e.getMessage(), e);
        }
    }
}