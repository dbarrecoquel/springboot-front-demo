package com.example.events.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.events.model.AddToBasketEvent;
import com.example.events.model.BasketViewEvent;

import jakarta.annotation.PostConstruct;
@Service
public class AddToBasketEventConsumer {
	 private static final Logger logger = LoggerFactory.getLogger(AddToBasketEventConsumer.class);
	    
	    @PostConstruct
	    public void init() {
	        logger.info("BasketViewEvent initialized and ready to consume events");
	    }
	    
	    @KafkaListener(
	        topics = "add-to-basket-events",
	        groupId = "add-to-basket-events-group",
	        containerFactory = "addToBasketKafkaListenerContainerFactory" 
	    )
	    public void consume(AddToBasketEvent event) {
	        logger.info("Received AddToBasketEvent: {}", event);
	        
	        try {
	            processEvent(event);
	            logger.info("AddToBasketEvent processed successfully: {}", event.getEventId());
	        } catch (Exception e) {
	            logger.error("Error processing AddToBasketEvent: {}", event.getEventId(), e);
	        }
	    }
	    
	    private void processEvent(AddToBasketEvent event) {
	        logger.info("Processing view for Basket: {} (ID: {})", 
	            event.getBasketId());
	            
	       
	    }
}
