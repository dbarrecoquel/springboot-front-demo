package com.example.events.producer;

import com.example.events.model.*;
import com.example.events.service.FailedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    private static final String TOPIC_PRODUCT        = "product-view-events";
    private static final String TOPIC_CATEGORY       = "category-view-events";
    private static final String TOPIC_BASKET_VIEW    = "basket-view-events";
    private static final String TOPIC_ADD_TO_BASKET  = "add-to-basket-events";
    private static final String TOPIC_BASKET_CREATED  = "basket-created";
    private static final String TOPIC_BASKET_CONVERTED = "basket-converted";
    private static final String TOPIC_BASKET_ABANDONED = "basket-abandoned";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FailedEventService failedEventService;

    public EventProducer(@Qualifier("genericKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate,
                          FailedEventService failedEventService) {
        this.kafkaTemplate = kafkaTemplate;
        this.failedEventService = failedEventService;
    }

    public void sendProductViewEvent(ProductViewEvent event) {
        send(TOPIC_PRODUCT, event.getProductId().toString(), event, "ProductViewEvent");
    }

    public void sendCategoryViewEvent(CategoryViewEvent event) {
        send(TOPIC_CATEGORY, event.getCategoryId().toString(), event, "CategoryViewEvent");
    }

    public void sendBasketViewEvent(BasketViewEvent event) {
        send(TOPIC_BASKET_VIEW, event.getBasketId().toString(), event, "BasketViewEvent");
    }

    public void sendAddToBasketEvent(AddToBasketEvent event) {
        send(TOPIC_ADD_TO_BASKET, event.getBasketId().toString(), event, "AddToBasketEvent");
    }

    public void sendBasketCreated(BasketCreatedEvent event) {
        send(TOPIC_BASKET_CREATED, event.getBasketId().toString(), event, "BasketCreatedEvent");
    }

    public void sendBasketConverted(BasketConvertedEvent event) {
        send(TOPIC_BASKET_CONVERTED, event.getBasketId().toString(), event, "BasketConvertedEvent");
    }

    public void sendBasketAbandoned(BasketAbandonedEvent event) {
        send(TOPIC_BASKET_ABANDONED, event.getBasketId().toString(), event, "BasketAbandonedEvent");
    }

    private void send(String topic, String key, Object event, String eventType) {
        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Event [{}] sent to [{}] key={} offset={} partition={}",
                    eventType, topic, key,
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition());
            } else {
                logger.error("Failed to send event [{}] to [{}] key={} error={}",
                    eventType, topic, key, ex.getMessage());
                // On ne peut pas récupérer l'eventId ici sans cast,
                // donc on passe key comme eventId (qui EST l'id métier)
                failedEventService.saveFailedEvent(key, eventType, topic, event, ex.getMessage());
            }
        });
    }
}