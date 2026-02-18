package com.example.events.consumer;

import com.example.events.config.KafkaConfig;
import com.example.events.model.ProductViewEvent;
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

@SpringBootTest(classes = {KafkaConfig.class, ProductEventConsumer.class})
@ActiveProfiles("test")
@EmbeddedKafka(
	    partitions = 1,
	    topics = {"product-view-events"}
	)
class ProductEventConsumerTest {
    
    private static final String TOPIC = "product-view-events";
    
    @MockitoSpyBean
    private ProductEventConsumer productEventConsumer;
    
    private KafkaTemplate<String, ProductViewEvent> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @BeforeEach
    void setUp() {
        // Configuration du producer de test
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        DefaultKafkaProducerFactory<String, ProductViewEvent> producerFactory = 
            new DefaultKafkaProducerFactory<>(configs);
        
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
        
        // Reset le spy avant chaque test
        reset(productEventConsumer);
    }
    
    @AfterEach
    void tearDown() {
        if (kafkaTemplate != null) {
            kafkaTemplate.destroy();
        }
    }
    
    @Test
    void testConsumeProductViewEvent_Success() {
        // Given
        ProductViewEvent event = new ProductViewEvent(
            1L,
            "iPhone 15 Pro",
            "IPHONE-15",
            null,
            "test-session-123",
            null
        );
        event.setIpAddress("192.168.1.1");
        event.setUserAgent("Mozilla/5.0");
        
        // When
        kafkaTemplate.send(TOPIC, "1", event);
        
        // Then - Attendre que le consumer traite le message
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(productEventConsumer, times(1)).consume(any(ProductViewEvent.class));
            });
        
        // Vérifier les détails de l'événement reçu
        ArgumentCaptor<ProductViewEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewEvent.class);
        verify(productEventConsumer).consume(eventCaptor.capture());
        
        ProductViewEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(1L, capturedEvent.getProductId());
        assertEquals("iPhone 15 Pro", capturedEvent.getProductName());
        assertEquals("IPHONE-15", capturedEvent.getProductSku());
        assertEquals("test-session-123", capturedEvent.getSessionId());
        assertEquals("192.168.1.1", capturedEvent.getIpAddress());
        assertEquals("Mozilla/5.0", capturedEvent.getUserAgent());
        assertNotNull(capturedEvent.getEventId());
        assertNotNull(capturedEvent.getTimestamp());
    }
    
    @Test
    void testConsumeProductViewEvent_WithUserData() {
        // Given
        ProductViewEvent event = new ProductViewEvent(
            2L,
            "Samsung Galaxy S24",
            "SAMSUNG-S24",
            42L,
            "test-session-456",
            "user@example.com"
        );
        
        // When
        kafkaTemplate.send(TOPIC, "2", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(productEventConsumer, times(1)).consume(any(ProductViewEvent.class));
            });
        
        ArgumentCaptor<ProductViewEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewEvent.class);
        verify(productEventConsumer).consume(eventCaptor.capture());
        
        ProductViewEvent capturedEvent = eventCaptor.getValue();
        assertEquals(2L, capturedEvent.getProductId());
        assertEquals(42L, capturedEvent.getUserId());
        assertEquals("user@example.com", capturedEvent.getUserEmail());
    }
    
    @Test
    void testConsumeProductViewEvent_AnonymousUser() {
        // Given
        ProductViewEvent event = new ProductViewEvent(
            3L,
            "Google Pixel 8",
            "PIXEL-8",
            null,  // userId null
            "anonymous-session",
            null   // userEmail null
        );
        
        // When
        kafkaTemplate.send(TOPIC, "3", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(productEventConsumer, times(1)).consume(any(ProductViewEvent.class));
            });
        
        ArgumentCaptor<ProductViewEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewEvent.class);
        verify(productEventConsumer).consume(eventCaptor.capture());
        
        ProductViewEvent capturedEvent = eventCaptor.getValue();
        assertNull(capturedEvent.getUserId());
        assertNull(capturedEvent.getUserEmail());
        assertEquals("anonymous-session", capturedEvent.getSessionId());
    }
    
    @Test
    void testConsumeMultipleProductViewEvents() {
        // Given
        int numberOfEvents = 5;
        
        // When
        for (int i = 1; i <= numberOfEvents; i++) {
            ProductViewEvent event = new ProductViewEvent(
                (long) i,
                "Product " + i,
                "SKU-" + i,
                null,
                "session-" + i,
                null
            );
            kafkaTemplate.send(TOPIC, String.valueOf(i), event);
        }
        
        // Then
        await()
            .atMost(15, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(productEventConsumer, times(numberOfEvents)).consume(any(ProductViewEvent.class));
            });
    }
    
    @Test
    void testConsumeProductViewEvent_VerifyTimestamp() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        ProductViewEvent event = new ProductViewEvent(
            1L,
            "Test Product",
            "TEST-SKU",
            null,
            "test-session",
            null
        );
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        // When
        kafkaTemplate.send(TOPIC, "1", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(productEventConsumer, times(1)).consume(any(ProductViewEvent.class));
            });
        
        ArgumentCaptor<ProductViewEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewEvent.class);
        verify(productEventConsumer).consume(eventCaptor.capture());
        
        LocalDateTime timestamp = eventCaptor.getValue().getTimestamp();
        assertNotNull(timestamp);
        assertTrue(timestamp.isAfter(before) || timestamp.isEqual(before));
        assertTrue(timestamp.isBefore(after) || timestamp.isEqual(after));
    }
    
    @Test
    void testConsumeProductViewEvent_AllFieldsPopulated() {
        // Given
        ProductViewEvent event = new ProductViewEvent(
            99L,
            "Complete Product",
            "COMPLETE-SKU",
            123L,
            "full-session-id",
            "complete@example.com"
        );
        event.setIpAddress("10.0.0.1");
        event.setUserAgent("TestAgent/1.0");
        
        // When
        kafkaTemplate.send(TOPIC, "99", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(productEventConsumer, times(1)).consume(any(ProductViewEvent.class));
            });
        
        ArgumentCaptor<ProductViewEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewEvent.class);
        verify(productEventConsumer).consume(eventCaptor.capture());
        
        ProductViewEvent capturedEvent = eventCaptor.getValue();
        
        // Vérifier tous les champs
        assertNotNull(capturedEvent.getEventId(), "Event ID should not be null");
        assertEquals(99L, capturedEvent.getProductId(), "Product ID mismatch");
        assertEquals("Complete Product", capturedEvent.getProductName(), "Product name mismatch");
        assertEquals("COMPLETE-SKU", capturedEvent.getProductSku(), "Product SKU mismatch");
        assertEquals(123L, capturedEvent.getUserId(), "User ID mismatch");
        assertEquals("full-session-id", capturedEvent.getSessionId(), "Session ID mismatch");
        assertEquals("complete@example.com", capturedEvent.getUserEmail(), "User email mismatch");
        assertEquals("10.0.0.1", capturedEvent.getIpAddress(), "IP address mismatch");
        assertEquals("TestAgent/1.0", capturedEvent.getUserAgent(), "User agent mismatch");
        assertNotNull(capturedEvent.getTimestamp(), "Timestamp should not be null");
    }
    
    @Test
    void testConsumerIsInitialized() {
        // Given & When
        // Le consumer est automatiquement initialisé par Spring
        
        // Then
        assertNotNull(productEventConsumer, "Consumer should be initialized");
    }
    
    @Test
    void testConsumeProductViewEvent_NoExceptionThrown() {
        // Given
        ProductViewEvent event = new ProductViewEvent(
            1L,
            "Safe Product",
            "SAFE-SKU",
            null,
            "safe-session",
            null
        );
        
        // When & Then - Ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            kafkaTemplate.send(TOPIC, "1", event);
            
            await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(productEventConsumer, times(1)).consume(any(ProductViewEvent.class));
                });
        });
    }
}
