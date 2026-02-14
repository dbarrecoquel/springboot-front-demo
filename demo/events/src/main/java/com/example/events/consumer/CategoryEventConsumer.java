package com.example.events.consumer;

import com.example.events.model.CategoryViewEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CategoryEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryEventConsumer.class);
    
    @PostConstruct
    public void init() {
        logger.info("🎧 CategoryEventConsumer initialized and ready to consume events");
    }
    
    @KafkaListener(
        topics = "category-view-events",
        groupId = "category-events-group",
        containerFactory = "categoryKafkaListenerContainerFactory"  // Utiliser le bon factory !
    )
    public void consume(CategoryViewEvent event) {
        logger.info("Received CategoryViewEvent: {}", event);
        
        try {
            processEvent(event);
            logger.info("CategoryViewEvent processed successfully: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Error processing CategoryViewEvent: {}", event.getEventId(), e);
        }
    }
    
    private void processEvent(CategoryViewEvent event) {
        logger.info("Processing view for Category: {} (ID: {}) | Subcategories: {} | Products: {} | User: {} | Depth: {}", 
            event.getCategoryName(),
            event.getCategoryId(),
            event.getSubcategoriesCount(),
            event.getProductsCount(),
            event.getUserEmail() != null ? event.getUserEmail() : "Anonymous",
            event.getDepthLevel());
            
        if (event.getBreadcrumbPath() != null) {
            logger.info("Navigation path: {}", event.getBreadcrumbPath());
        }
    }
}