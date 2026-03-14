package com.example.events.consumer;

import com.example.events.model.BasketCreatedEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class BasketCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BasketCreatedConsumer.class);

    private final ConcurrentHashMap<String, BasketCreatedEvent> pendingBaskets = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("BasketCreatedConsumer initialized");
    }

    @KafkaListener(
        topics = "basket-created",
        groupId = "basket-reconciliation-group",
        containerFactory = "basketCreatedKafkaListenerContainerFactory"
    )
    public void consume(BasketCreatedEvent event) {
        logger.info("Received BasketCreatedEvent: basketId={}", event.getBasketId());
        pendingBaskets.put(event.getBasketId().toString(), event);
    }

    public void markAsConverted(String basketId) {
        BasketCreatedEvent removed = pendingBaskets.remove(basketId);
        if (removed != null) {
            logger.info("Basket {} removed from pending (converted)", basketId);
        }
    }

    public ConcurrentHashMap<String, BasketCreatedEvent> getPendingBaskets() {
        return pendingBaskets;
    }
}