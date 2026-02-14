package com.example.events.producer;

import com.example.events.model.ProductViewEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ProductEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventProducer.class);
    private static final String TOPIC = "product-view-events";
    
    private final KafkaTemplate<String, ProductViewEvent> kafkaTemplate;
    
    public ProductEventProducer(KafkaTemplate<String, ProductViewEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendProductViewEvent(ProductViewEvent event) {
        logger.info("📤 Sending ProductViewEvent: eventId={}, productId={}, productName={}", 
            event.getEventId(), event.getProductId(), event.getProductName());
        
        try {
        	//utilise KafkaTemplate pour expédier des données de manière asynchrone.
        	//topic : destination, productid : clé, event: la valeur
            CompletableFuture<SendResult<String, ProductViewEvent>> future = 
                kafkaTemplate.send(TOPIC, event.getProductId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                	// metrics et monitoring.
                    logger.info("✅ ProductViewEvent sent successfully: eventId={}, offset={}, partition={}", 
                        event.getEventId(), 
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
                } else {
                	//retry ou stockage en bdd pour renvoie plus tard
                    logger.error("❌ Failed to send ProductViewEvent: eventId={}, error={}", 
                        event.getEventId(), ex.getMessage(), ex);
                }
            });
            
            // Attendre un peu pour vérifier les erreurs immédiates
            future.get(5, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            logger.error("❌ Error sending ProductViewEvent: eventId={}, error={}", 
                event.getEventId(), e.getMessage(), e);
        }
    }
}