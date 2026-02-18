package com.example.events.producer;

import com.example.events.model.ProductViewEvent;
import com.example.events.service.FailedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ProductEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventProducer.class);
    private static final String TOPIC = "product-view-events";
    
    private final KafkaTemplate<String, ProductViewEvent> kafkaTemplate;
    private final FailedEventService failedEventService;
    
    public ProductEventProducer(KafkaTemplate<String, ProductViewEvent> kafkaTemplate,
                               FailedEventService failedEventService) {
        this.kafkaTemplate = kafkaTemplate;
        this.failedEventService = failedEventService;
    }
    
    public void sendProductViewEvent(ProductViewEvent event) {
        logger.info("📤 Sending ProductViewEvent: eventId={}, productId={}, productName={}", 
            event.getEventId(), event.getProductId(), event.getProductName());
        
        CompletableFuture<SendResult<String, ProductViewEvent>> future = 
            kafkaTemplate.send(TOPIC, event.getProductId().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // Succès - Metrics et monitoring
                logger.info("ProductViewEvent sent successfully: eventId={}, offset={}, partition={}", 
                    event.getEventId(), 
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition());
                
                // TODO: Envoyer des métriques à Prometheus/Grafana
                // metricsService.incrementSuccessCounter("product-view-events");
                
            } else {
                //  Échec - Sauvegarder pour retry
                logger.error("Failed to send ProductViewEvent: eventId={}, error={}", 
                    event.getEventId(), ex.getMessage());
                
                // Sauvegarder l'événement échoué en base
                failedEventService.saveFailedEvent(
                    event.getEventId(),
                    "ProductViewEvent",
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