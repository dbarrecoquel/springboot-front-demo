package com.example.events.producer;

import com.example.events.config.KafkaConfig;
import com.example.events.model.ProductViewEvent;
import com.example.events.service.FailedEventService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {KafkaConfig.class, EventProducer.class})
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = {"product-view-events"}
)
class ProductEventProducerTest {
    
    private static final String TOPIC = "product-view-events";
    
    @Autowired
    private EventProducer productEventProducer;
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @MockitoBean
    private FailedEventService failedEventService;
    
    private KafkaMessageListenerContainer<String, ProductViewEvent> container;
    private BlockingQueue<ConsumerRecord<String, ProductViewEvent>> records;
    
    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductViewEvent.class.getName());
        
        DefaultKafkaConsumerFactory<String, ProductViewEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(configs);
        
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, ProductViewEvent>) records::add);
        container.start();
        
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }
    
    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }
    
    @Test
    void testSendProductViewEvent_Success() throws InterruptedException {
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
        productEventProducer.sendProductViewEvent(event);
        
        // Then
        ConsumerRecord<String, ProductViewEvent> received = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received, "Should receive a message");
        assertEquals("1", received.key(), "Key should be product ID");
        
        ProductViewEvent receivedEvent = received.value();
        assertNotNull(receivedEvent, "Event should not be null");
        assertEquals(1L, receivedEvent.getProductId());
        assertEquals("iPhone 15 Pro", receivedEvent.getProductName());
        assertEquals("IPHONE-15", receivedEvent.getProductSku());
        assertEquals("test-session-123", receivedEvent.getSessionId());
        assertEquals("192.168.1.1", receivedEvent.getIpAddress());
        assertNotNull(receivedEvent.getEventId());
        assertNotNull(receivedEvent.getTimestamp());
    }
    
    @Test
    void testSendProductViewEvent_WithUserData() throws InterruptedException {
        ProductViewEvent event = new ProductViewEvent(
            2L,
            "Samsung Galaxy S24",
            "SAMSUNG-S24",
            42L,
            "test-session-456",
            "user@example.com"
        );
        
        productEventProducer.sendProductViewEvent(event);
        
        ConsumerRecord<String, ProductViewEvent> received = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received);
        ProductViewEvent receivedEvent = received.value();
        
        assertEquals(2L, receivedEvent.getProductId());
        assertEquals(42L, receivedEvent.getUserId());
        assertEquals("user@example.com", receivedEvent.getUserEmail());
    }
    
    @Test
    void testSendProductViewEvent_MultipleEvents() throws InterruptedException {
        int numberOfEvents = 5;
        
        for (int i = 1; i <= numberOfEvents; i++) {
            ProductViewEvent event = new ProductViewEvent(
                (long) i,
                "Product " + i,
                "SKU-" + i,
                null,
                "session-" + i,
                null
            );
            productEventProducer.sendProductViewEvent(event);
        }
        
        for (int i = 0; i < numberOfEvents; i++) {
            ConsumerRecord<String, ProductViewEvent> received = records.poll(10, TimeUnit.SECONDS);
            assertNotNull(received, "Should receive event " + (i + 1));
        }
        
        ConsumerRecord<String, ProductViewEvent> extra = records.poll(2, TimeUnit.SECONDS);
        assertNull(extra, "Should not receive extra events");
    }
    
    @Test
    void testEventIdIsUnique() throws InterruptedException {
        ProductViewEvent event1 = new ProductViewEvent(1L, "Product 1", "SKU-1", null, "session-1", null);
        ProductViewEvent event2 = new ProductViewEvent(1L, "Product 1", "SKU-1", null, "session-1", null);
        
        productEventProducer.sendProductViewEvent(event1);
        productEventProducer.sendProductViewEvent(event2);
        
        ConsumerRecord<String, ProductViewEvent> received1 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, ProductViewEvent> received2 = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received1);
        assertNotNull(received2);
        
        String eventId1 = received1.value().getEventId();
        String eventId2 = received2.value().getEventId();
        
        assertNotEquals(eventId1, eventId2, "Event IDs should be unique");
    }
    
    @Test
    void testTimestampIsSet() throws InterruptedException {
    	LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        ProductViewEvent event = new ProductViewEvent(1L, "Product", "SKU", null, "session", null);
       
        
        productEventProducer.sendProductViewEvent(event);
        
        ConsumerRecord<String, ProductViewEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertNotNull(received);
        
        LocalDateTime timestamp = received.value().getTimestamp();
        assertNotNull(timestamp);
        assertTrue(timestamp.isAfter(before));
    }
}