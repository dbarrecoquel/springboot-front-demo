package com.example.events.consumer;

import com.example.events.model.ProductViewEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventConsumer.class);
    
    @PostConstruct
    public void init() {
        logger.info("🎧 ProductEventConsumer initialized and ready to consume events");
    }
    
    @KafkaListener(
        topics = "product-view-events",
        groupId = "product-events-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ProductViewEvent event) {
        logger.info(" Received ProductViewEvent: {}", event);
        
        try {
            // Traiter l'événement
            processEvent(event);
            
            logger.info("ProductViewEvent processed successfully: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Error processing ProductViewEvent: {}", event.getEventId(), e);
        }
    }
    
    private void processEvent(ProductViewEvent event) {
        logger.info(" Processing view for Product: {} (SKU: {}) by User: {}", 
            event.getProductName(),
            event.getProductSku(),
            event.getUserEmail() != null ? event.getUserEmail() : "Anonymous");
            
        //incrémenter un compteur, enregistrer dans une DB analytics, etc.
    }
}