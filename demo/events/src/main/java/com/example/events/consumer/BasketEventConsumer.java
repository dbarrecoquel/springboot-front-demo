package com.example.events.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.events.model.BasketViewEvent;

import jakarta.annotation.PostConstruct;
@Service
public class BasketEventConsumer {
	 private static final Logger logger = LoggerFactory.getLogger(BasketEventConsumer.class);
	    
	    @PostConstruct
	    public void init() {
	        logger.info("BasketViewEvent initialized and ready to consume events");
	    }
	    
	    @KafkaListener(
	        topics = "basket-view-events",
	        groupId = "basket-events-group",
	        containerFactory = "basketKafkaListenerContainerFactory" 
	    )
	    public void consume(BasketViewEvent event) {
	        logger.info("Received BasketViewEvent: {}", event);
	        
	        try {
	            processEvent(event);
	            logger.info("BasketViewEvent processed successfully: {}", event.getEventId());
	        } catch (Exception e) {
	            logger.error("Error processing BasketViewEvent: {}", event.getEventId(), e);
	        }
	    }
	    
	    private void processEvent(BasketViewEvent event) {
	        logger.info("Processing view for Basket: {} (ID: {})", 
	            event.getBasketId());
	            
	       
	    }
}
