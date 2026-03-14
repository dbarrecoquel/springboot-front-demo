package com.example.events.consumer;

import com.example.events.model.BasketAbandonedEvent;
import com.example.events.service.MailService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BasketAbandonedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BasketAbandonedConsumer.class);

    private final MailService mailService;

    public BasketAbandonedConsumer(MailService mailService) {
        this.mailService = mailService;
    }

    @PostConstruct
    public void init() {
        logger.info("BasketAbandonedConsumer initialized");
    }

    @KafkaListener(
        topics = "basket-abandoned",
        groupId = "basket-abandoned-group",
        containerFactory = "basketAbandonedKafkaListenerContainerFactory"
    )
    public void consume(BasketAbandonedEvent event) {
        logger.info("Received BasketAbandonedEvent: basketId={} email={}", 
            event.getBasketId(), event.getUserEmail());
        try {
            mailService.sendAbandonedBasketMail(event.getUserEmail(), event.getBasketId().toString());
        } catch (Exception e) {
            logger.error("Failed to send abandoned mail for basket {}", event.getBasketId(), e);
        }
    }
}