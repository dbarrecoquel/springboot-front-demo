package com.example.events.producer;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.events.model.BasketViewEvent;
import com.example.events.service.FailedEventService;
@Service
public class BasketEventProducer {
	 private static final Logger logger = LoggerFactory.getLogger(BasketEventProducer.class);
	    private static final String TOPIC = "basket-view-events";
	    
	    private final KafkaTemplate<String, BasketViewEvent> kafkaTemplate;
	    private final FailedEventService failedEventService;
	    
	    public BasketEventProducer(@Qualifier("basketKafkaTemplate") KafkaTemplate<String, BasketViewEvent> kafkaTemplate,
	                                FailedEventService failedEventService) {
	        this.kafkaTemplate = kafkaTemplate;
	        this.failedEventService = failedEventService;
	    }
	    
	    public void sendBasketViewEvent(BasketViewEvent event) {
	        logger.info("Sending BasketViewEvent: eventId={}, basketID={}", 
	            event.getEventId(), event.getBasketId());
	        
	        CompletableFuture<SendResult<String, BasketViewEvent>> future = 
	            kafkaTemplate.send(TOPIC, event.getBasketId().toString(), event);
	        
	        future.whenComplete((result, ex) -> {
	            if (ex == null) {
	                // Succès - Metrics et monitoring
	                logger.info("BasketViewEvent sent successfully: eventId={}, offset={}, partition={}", 
	                    event.getEventId(), 
	                    result.getRecordMetadata().offset(),
	                    result.getRecordMetadata().partition());
	                
	                // TODO: Envoyer des métriques à Prometheus/Grafana
	                // metricsService.incrementSuccessCounter("category-view-events");
	                
	            } else {
	                // Échec - Sauvegarder pour retry
	                logger.error("Failed to send BasketViewEvent: eventId={}, error={}", 
	                    event.getEventId(), ex.getMessage());
	                
	                // Sauvegarder l'événement échoué en base
	                failedEventService.saveFailedEvent(
	                    event.getEventId(),
	                    "BasketViewEvent",
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
