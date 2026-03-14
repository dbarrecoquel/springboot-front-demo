package com.example.events.service;

import com.example.events.model.*;
import com.example.events.producer.EventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventRetryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EventRetryScheduler.class);

    private final FailedEventService failedEventService;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;

    public EventRetryScheduler(FailedEventService failedEventService,
                                EventProducer eventProducer,
                                ObjectMapper objectMapper) {
        this.failedEventService = failedEventService;
        this.eventProducer = eventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void retryFailedEvents() {
        logger.info("Starting retry of failed events...");

        List<FailedEvent> eventsToRetry = failedEventService.getEventsToRetry(2);

        if (eventsToRetry.isEmpty()) {
            logger.info("No failed events to retry");
            return;
        }

        logger.info("Found {} events to retry", eventsToRetry.size());
        eventsToRetry.forEach(this::retryEvent);
        failedEventService.logStatistics();
    }

    private void retryEvent(FailedEvent failedEvent) {
        try {
            logger.info("Retrying event: eventId={}, type={}, attempt={}/{}",
                failedEvent.getEventId(), failedEvent.getEventType(),
                failedEvent.getRetryCount() + 1, failedEvent.getMaxRetries());

            switch (failedEvent.getEventType()) {
                case "ProductViewEvent" -> {
                    ProductViewEvent e = objectMapper.readValue(failedEvent.getPayload(), ProductViewEvent.class);
                    eventProducer.sendProductViewEvent(e);
                    failedEventService.markAsSuccess(failedEvent);
                }
                case "CategoryViewEvent" -> {
                    CategoryViewEvent e = objectMapper.readValue(failedEvent.getPayload(), CategoryViewEvent.class);
                    eventProducer.sendCategoryViewEvent(e);
                    failedEventService.markAsSuccess(failedEvent);
                }
                case "BasketViewEvent" -> {
                    BasketViewEvent e = objectMapper.readValue(failedEvent.getPayload(), BasketViewEvent.class);
                    eventProducer.sendBasketViewEvent(e);
                    failedEventService.markAsSuccess(failedEvent);
                }
                case "AddToBasketEvent" -> {
                    AddToBasketEvent e = objectMapper.readValue(failedEvent.getPayload(), AddToBasketEvent.class);
                    eventProducer.sendAddToBasketEvent(e);
                    failedEventService.markAsSuccess(failedEvent);
                }
                case "BasketCreatedEvent" -> {
                    BasketCreatedEvent e = objectMapper.readValue(failedEvent.getPayload(), BasketCreatedEvent.class);
                    eventProducer.sendBasketCreated(e);
                    failedEventService.markAsSuccess(failedEvent);
                }
                case "BasketConvertedEvent" -> {
                    BasketConvertedEvent e = objectMapper.readValue(failedEvent.getPayload(), BasketConvertedEvent.class);
                    eventProducer.sendBasketConverted(e);
                    failedEventService.markAsSuccess(failedEvent);
                }
                default -> {
                    logger.error("Unknown event type: {}", failedEvent.getEventType());
                    failedEvent.markAsFailed();
                }
            }

        } catch (Exception e) {
            logger.error("Retry failed for event: eventId={}, error={}", failedEvent.getEventId(), e.getMessage());
            failedEventService.incrementRetry(failedEvent, e.getMessage());
        }
    }
}