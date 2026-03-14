package com.example.events.consumer;


import com.example.events.model.BasketConvertedEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BasketConvertedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BasketConvertedConsumer.class);

    private final BasketCreatedConsumer basketCreatedConsumer;

    public BasketConvertedConsumer(BasketCreatedConsumer basketCreatedConsumer) {
        this.basketCreatedConsumer = basketCreatedConsumer;
    }

    @PostConstruct
    public void init() {
        logger.info("BasketConvertedConsumer initialized");
    }

    @KafkaListener(
        topics = "basket-converted",
        groupId = "basket-reconciliation-group",
        containerFactory = "basketConvertedKafkaListenerContainerFactory"
    )
    public void consume(BasketConvertedEvent event) {
        logger.info("Received BasketConvertedEvent: basketId={}", event.getBasketId());
        basketCreatedConsumer.markAsConverted(event.getBasketId().toString());
    }
}
