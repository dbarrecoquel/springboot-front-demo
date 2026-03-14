package com.example.events.service;

import com.example.events.consumer.BasketCreatedConsumer;
import com.example.events.model.BasketAbandonedEvent;
import com.example.events.model.BasketCreatedEvent;
import com.example.events.producer.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReconciliationService {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);
    private static final long TTL_HOURS = 24;

    private final BasketCreatedConsumer basketCreatedConsumer;
    private final EventProducer eventProducer;

    public ReconciliationService(BasketCreatedConsumer basketCreatedConsumer,
                                  EventProducer eventProducer) {
        this.basketCreatedConsumer = basketCreatedConsumer;
        this.eventProducer = eventProducer;
    }

    @Scheduled(fixedRate = 3600000) // toutes les heures
    public void reconcile() {
        logger.info("Starting basket reconciliation...");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(TTL_HOURS);
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, BasketCreatedEvent> entry :
                basketCreatedConsumer.getPendingBaskets().entrySet()) {

            BasketCreatedEvent basket = entry.getValue();

            if (basket.getCreatedAt().isBefore(cutoff)) {
                logger.info("Basket {} is abandoned (created at {})",
                    basket.getBasketId(), basket.getCreatedAt());

                BasketAbandonedEvent abandonedEvent = new BasketAbandonedEvent(
                    basket.getBasketId(), basket.getUserEmail()
                );
                eventProducer.sendBasketAbandoned(abandonedEvent);
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(basketCreatedConsumer.getPendingBaskets()::remove);
        logger.info("Reconciliation done. {} baskets marked as abandoned.", toRemove.size());
    }
}