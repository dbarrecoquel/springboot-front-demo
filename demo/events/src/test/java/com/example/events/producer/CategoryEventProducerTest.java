package com.example.events.producer;


import com.example.events.config.KafkaConfig;
import com.example.events.model.CategoryViewEvent;
import com.example.events.service.FailedEventService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {KafkaConfig.class, CategoryEventProducer.class})
@ActiveProfiles("test")
@EmbeddedKafka(
	    partitions = 1,
	    topics = {"category-view-events"}
	)
	class CategoryEventProducerTest {
	    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    private static final String TOPIC = "category-view-events";
    
    @Autowired
    private CategoryEventProducer categoryEventProducer;
    
    @MockitoBean
    private FailedEventService failedEventService;
    
    private KafkaMessageListenerContainer<String, CategoryViewEvent> container;
    private BlockingQueue<ConsumerRecord<String, CategoryViewEvent>> records;
    
    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test-category-group");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CategoryViewEvent.class.getName());
        
        DefaultKafkaConsumerFactory<String, CategoryViewEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(configs);
        
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, CategoryViewEvent>) records::add);
        container.start();
        
        ContainerTestUtils.waitForAssignment(container, 1);
    }
    
    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }
    
    @Test
    void testSendCategoryViewEvent_Success() throws InterruptedException {
        // Given
        CategoryViewEvent event = new CategoryViewEvent(
            1L,                    // categoryId
            "Électronique",        // categoryName
            null,                  // parentCategoryId
            null,                  // parentCategoryName
            0,                     // depthLevel
            3,                     // subcategoriesCount
            15,                    // productsCount
            null,                  // userId
            "test-session-789",    // sessionId
            null                   // userEmail
        );
        event.setBreadcrumbPath("Électronique");
        
        // When
        categoryEventProducer.sendCategoryViewEvent(event);
        
        // Then
        ConsumerRecord<String, CategoryViewEvent> received = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received, "Should receive a message");
        assertEquals("1", received.key(), "Key should be category ID");
        
        CategoryViewEvent receivedEvent = received.value();
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getCategoryId());
        assertEquals("Électronique", receivedEvent.getCategoryName());
        assertEquals(0, receivedEvent.getDepthLevel());
        assertEquals(3, receivedEvent.getSubcategoriesCount());
        assertEquals(15, receivedEvent.getProductsCount());
        assertEquals("Électronique", receivedEvent.getBreadcrumbPath());
        assertNotNull(receivedEvent.getEventId());
        assertNotNull(receivedEvent.getTimestamp());
    }
    
    @Test
    void testSendCategoryViewEvent_WithParentCategory() throws InterruptedException {
        // Given
        CategoryViewEvent event = new CategoryViewEvent(
            3L,                    // categoryId
            "Smartphones",         // categoryName
            1L,                    // parentCategoryId
            "Électronique",        // parentCategoryName
            1,                     // depthLevel
            2,                     // subcategoriesCount
            8,                     // productsCount
            42L,                   // userId
            "session-abc",         // sessionId
            "user@example.com"     // userEmail
        );
        event.setBreadcrumbPath("Électronique > Smartphones");
        event.setIpAddress("192.168.1.100");
        
        // When
        categoryEventProducer.sendCategoryViewEvent(event);
        
        // Then
        ConsumerRecord<String, CategoryViewEvent> received = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received);
        CategoryViewEvent receivedEvent = received.value();
        
        assertEquals(3L, receivedEvent.getCategoryId());
        assertEquals("Smartphones", receivedEvent.getCategoryName());
        assertEquals(1L, receivedEvent.getParentCategoryId());
        assertEquals("Électronique", receivedEvent.getParentCategoryName());
        assertEquals(1, receivedEvent.getDepthLevel());
        assertEquals("Électronique > Smartphones", receivedEvent.getBreadcrumbPath());
        assertEquals(42L, receivedEvent.getUserId());
        assertEquals("user@example.com", receivedEvent.getUserEmail());
        assertEquals("192.168.1.100", receivedEvent.getIpAddress());
    }
    
    @Test
    void testSendCategoryViewEvent_DeepHierarchy() throws InterruptedException {
        // Given
        CategoryViewEvent event = new CategoryViewEvent(
            5L,
            "iPhone",
            3L,
            "Smartphones",
            2,
            0,
            5,
            null,
            "session-xyz",
            null
        );
        event.setBreadcrumbPath("Électronique > Smartphones > iPhone");
        
        // When
        categoryEventProducer.sendCategoryViewEvent(event);
        
        // Then
        ConsumerRecord<String, CategoryViewEvent> received = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received);
        CategoryViewEvent receivedEvent = received.value();
        
        assertEquals(2, receivedEvent.getDepthLevel());
        assertEquals("Électronique > Smartphones > iPhone", receivedEvent.getBreadcrumbPath());
    }
    
    @Test
    void testMultipleCategoryEvents() throws InterruptedException {
        // Given
        CategoryViewEvent event1 = new CategoryViewEvent(1L, "Cat1", null, null, 0, 2, 10, null, "s1", null);
        CategoryViewEvent event2 = new CategoryViewEvent(2L, "Cat2", null, null, 0, 3, 15, null, "s2", null);
        CategoryViewEvent event3 = new CategoryViewEvent(3L, "Cat3", null, null, 0, 1, 5, null, "s3", null);
        
        // When
        categoryEventProducer.sendCategoryViewEvent(event1);
        categoryEventProducer.sendCategoryViewEvent(event2);
        categoryEventProducer.sendCategoryViewEvent(event3);
        
        // Then
        ConsumerRecord<String, CategoryViewEvent> r1 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, CategoryViewEvent> r2 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, CategoryViewEvent> r3 = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        
        assertEquals("Cat1", r1.value().getCategoryName());
        assertEquals("Cat2", r2.value().getCategoryName());
        assertEquals("Cat3", r3.value().getCategoryName());
    }
}