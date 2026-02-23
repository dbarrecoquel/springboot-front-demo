package com.example.events.consumer;

import com.example.events.config.KafkaConfig;
import com.example.events.model.BasketViewEvent;
import com.example.events.model.CategoryViewEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {KafkaConfig.class, BasketEventConsumer.class})
@ActiveProfiles("test")
@EmbeddedKafka(
	    partitions = 1,
	    topics = {"basket-view-events"}
	)
class BasketEventConsumerTest {
    
    private static final String TOPIC = "basket-view-events";
    
    @MockitoSpyBean
    private BasketEventConsumer basketEventConsumer;
    
    private KafkaTemplate<String, BasketViewEvent> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @BeforeEach
    void setUp() {
        // Configuration du producer de test
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        DefaultKafkaProducerFactory<String, BasketViewEvent> producerFactory = 
            new DefaultKafkaProducerFactory<>(configs);
        
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
        
        // Reset le spy avant chaque test
        reset(basketEventConsumer);
    }
    
    @AfterEach
    void tearDown() {
        if (kafkaTemplate != null) {
            kafkaTemplate.destroy();
        }
    }
    
    @Test
    void testConsumeBasketViewEvent() {
        // Given
        BasketViewEvent event = new BasketViewEvent( );
        event.setBasketId(1L);
        event.setSessionId("test");
        event.setUserId(1L);
        LocalDateTime now = LocalDateTime.now();
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        
        
        // When
        kafkaTemplate.send(TOPIC, "1", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(basketEventConsumer, times(1)).consume(any(BasketViewEvent.class));
            });
        
        ArgumentCaptor<BasketViewEvent> eventCaptor = ArgumentCaptor.forClass(BasketViewEvent.class);
        verify(basketEventConsumer).consume(eventCaptor.capture());
        
        BasketViewEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(1L, capturedEvent.getBasketId());
        assertEquals("test", capturedEvent.getSessionId());
        assertEquals(1L, capturedEvent.getUserId());
        assertNotNull(capturedEvent.getEventId());
        assertNotNull(capturedEvent.getCreatedAt());
        assertNotNull(capturedEvent.getUpdatedAt());
    }
    
  
   
    
   
    @Test
    void testConsumeMultipleBasketViewEvents() {
        // Given
        int numberOfEvents = 5;
        
        // When
        for (int i = 1; i <= numberOfEvents; i++) {
	    	 BasketViewEvent event = new BasketViewEvent( );
	         event.setBasketId(1L);
	         event.setSessionId("test");
	         event.setUserId(1L);
	         LocalDateTime now = LocalDateTime.now();
	         event.setCreatedAt(now);
	         event.setUpdatedAt(now);
             kafkaTemplate.send(TOPIC, String.valueOf(i), event);
        }
        
        // Then
        await()
            .atMost(15, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(basketEventConsumer, times(numberOfEvents)).consume(any(BasketViewEvent.class));
            });
    }
    
  
  
    
    @Test
    void testConsumerIsInitialized() {
        // Given & When
        // Le consumer est automatiquement initialisé par Spring
        
        // Then
        assertNotNull(basketEventConsumer, "Consumer should be initialized");
    }
    
    @Test
    void testConsumeCategoryViewEvent_NoExceptionThrown() {
        // Given
    	 BasketViewEvent event = new BasketViewEvent( );
         event.setBasketId(1L);
         event.setSessionId("test");
         event.setUserId(1L);
         LocalDateTime now = LocalDateTime.now();
         event.setCreatedAt(now);
         event.setUpdatedAt(now);
        
        // When & Then - Ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            kafkaTemplate.send(TOPIC, "1", event);
            
            await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(basketEventConsumer, times(1)).consume(any(BasketViewEvent.class));
                });
        });
    }
}