package com.example.events.integration;

import com.example.events.config.KafkaConfig;
import com.example.events.model.BasketViewEvent;
import com.example.events.model.CategoryViewEvent;
import com.example.events.model.ProductViewEvent;
import com.example.events.producer.EventProducer;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
    KafkaConfig.class,
    EventProducer.class
})
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = {"product-view-events", "category-view-events","basket-view-events"}
)
@DirtiesContext
class KafkaIntegrationTest {
    
    @Autowired
    private EventProducer eventProducer;
    
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @MockitoBean
    private FailedEventService failedEventService;
    
    private KafkaMessageListenerContainer<String, ProductViewEvent> productContainer;
    private BlockingQueue<ConsumerRecord<String, ProductViewEvent>> productRecords;
    
    private KafkaMessageListenerContainer<String, CategoryViewEvent> categoryContainer;
    private BlockingQueue<ConsumerRecord<String, CategoryViewEvent>> categoryRecords;
    
    private KafkaMessageListenerContainer<String, BasketViewEvent> basketContainer;
    private BlockingQueue<ConsumerRecord<String, BasketViewEvent>> basketRecords;
    
    @BeforeEach
    void setUp() {
        // Setup Product Consumer
        Map<String, Object> productConfigs = new HashMap<>();
        productConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        productConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, "product-integration-test-group");
        productConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        productConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        productConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        productConfigs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        productConfigs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductViewEvent.class.getName());
        
        DefaultKafkaConsumerFactory<String, ProductViewEvent> productConsumerFactory = 
            new DefaultKafkaConsumerFactory<>(productConfigs);
        
        ContainerProperties productContainerProperties = new ContainerProperties("product-view-events");
        productContainer = new KafkaMessageListenerContainer<>(productConsumerFactory, productContainerProperties);
        
        productRecords = new LinkedBlockingQueue<>();
        productContainer.setupMessageListener((MessageListener<String, ProductViewEvent>) productRecords::add);
        productContainer.start();
        
        ContainerTestUtils.waitForAssignment(productContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        
        // Setup Category Consumer
        Map<String, Object> categoryConfigs = new HashMap<>();
        categoryConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        categoryConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, "category-integration-test-group");
        categoryConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        categoryConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        categoryConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        categoryConfigs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        categoryConfigs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CategoryViewEvent.class.getName());
        
        DefaultKafkaConsumerFactory<String, CategoryViewEvent> categoryConsumerFactory = 
            new DefaultKafkaConsumerFactory<>(categoryConfigs);
        
        ContainerProperties categoryContainerProperties = new ContainerProperties("category-view-events");
        categoryContainer = new KafkaMessageListenerContainer<>(categoryConsumerFactory, categoryContainerProperties);
        
        categoryRecords = new LinkedBlockingQueue<>();
        categoryContainer.setupMessageListener((MessageListener<String, CategoryViewEvent>) categoryRecords::add);
        categoryContainer.start();
        
        ContainerTestUtils.waitForAssignment(categoryContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        
        //SETUP BASKET CONSUMER
        Map<String, Object> basketConfigs = new HashMap<>();
        basketConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        basketConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, "basket-integration-test-group");
        basketConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        basketConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        basketConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        basketConfigs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        basketConfigs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BasketViewEvent.class.getName());
        
        DefaultKafkaConsumerFactory<String, BasketViewEvent> basketConsumerFactory = 
            new DefaultKafkaConsumerFactory<>(basketConfigs);
        
        ContainerProperties basketContainerProperties = new ContainerProperties("basket-view-events");
        basketContainer = new KafkaMessageListenerContainer<>(basketConsumerFactory, basketContainerProperties);
        
        basketRecords = new LinkedBlockingQueue<>();
        basketContainer.setupMessageListener((MessageListener<String, BasketViewEvent>) basketRecords::add);
        basketContainer.start();
        
        ContainerTestUtils.waitForAssignment(basketContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        
        
    }
    
    @AfterEach
    void tearDown() {
        if (productContainer != null) {
            productContainer.stop();
        }
        if (categoryContainer != null) {
            categoryContainer.stop();
        }
        if (basketContainer != null)
        	basketContainer.stop();
    }
    
    @Test
    void testProductEventEndToEnd() throws InterruptedException {
        // Given
        ProductViewEvent event = new ProductViewEvent(
            1L, "Test Product", "TEST-SKU", null, "session-123", null
        );
        event.setIpAddress("192.168.1.1");
        
        // When
        eventProducer.sendProductViewEvent(event);
        
        // Then
        ConsumerRecord<String, ProductViewEvent> received = productRecords.poll(15, TimeUnit.SECONDS);
        
        assertNotNull(received, "Should receive ProductViewEvent");
        assertEquals("1", received.key(), "Key should be product ID");
        
        ProductViewEvent receivedEvent = received.value();
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getProductId());
        assertEquals("Test Product", receivedEvent.getProductName());
        assertEquals("TEST-SKU", receivedEvent.getProductSku());
        assertEquals("session-123", receivedEvent.getSessionId());
        assertEquals("192.168.1.1", receivedEvent.getIpAddress());
        assertNotNull(receivedEvent.getEventId());
        assertNotNull(receivedEvent.getTimestamp());
    }
    
    @Test
    void testCategoryEventEndToEnd() throws InterruptedException {
        // Given
        CategoryViewEvent event = new CategoryViewEvent(
            1L, "Test Category", null, null, 0, 2, null, "session-456", null
        );
        event.setBreadcrumbPath("Test Category");
        
        // When
        eventProducer.sendCategoryViewEvent(event);
        
        // Then
        ConsumerRecord<String, CategoryViewEvent> received = categoryRecords.poll(15, TimeUnit.SECONDS);
        
        assertNotNull(received, "Should receive CategoryViewEvent");
        assertEquals("1", received.key(), "Key should be category ID");
        
        CategoryViewEvent receivedEvent = received.value();
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getCategoryId());
        assertEquals("Test Category", receivedEvent.getCategoryName());
        assertEquals(0, receivedEvent.getDepthLevel());
        assertEquals(2, receivedEvent.getSubcategoriesCount());
        assertEquals("Test Category", receivedEvent.getBreadcrumbPath());
        assertNotNull(receivedEvent.getEventId());
        assertNotNull(receivedEvent.getTimestamp());
    }
    @Test
    void testBasketEventEndToEnd() throws InterruptedException {
        // Given
    	 LocalDateTime now = LocalDateTime.now();
    	 BasketViewEvent event1 = new BasketViewEvent(
                 1L,                    // basketid
                 1L,					   // userId
                 "test",					// session id
                 now,					//createdAt
                 now							//updatedAt	
             );
        // When
    	 eventProducer.sendBasketViewEvent(event1);
        
        // Then
        ConsumerRecord<String, BasketViewEvent> received = basketRecords.poll(15, TimeUnit.SECONDS);
        
        assertNotNull(received, "Should receive BasketViewEvent");
        assertEquals("1", received.key(), "Key should be basket ID");
        
       
        BasketViewEvent receivedEvent = received.value();
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getBasketId());
        assertEquals(1L, receivedEvent.getUserId());
        assertEquals("test", receivedEvent.getSessionId());
        assertNotNull(receivedEvent.getEventId());
        assertNotNull(receivedEvent.getCreatedAt());
        assertNotNull(receivedEvent.getUpdatedAt());
    }
    
    @Test
    void testBothEventsInSequence() throws InterruptedException {
        // Given
        ProductViewEvent productEvent = new ProductViewEvent(
            99L, "Product 99", "SKU-99", null, "session-999", null
        );
        
        CategoryViewEvent categoryEvent = new CategoryViewEvent(
            88L, "Category 88", null, null, 0, 1, null, "session-888", null
        );
        
        // When
        eventProducer.sendProductViewEvent(productEvent);
        eventProducer.sendCategoryViewEvent(categoryEvent);
        
        // Then
        ConsumerRecord<String, ProductViewEvent> receivedProduct = productRecords.poll(15, TimeUnit.SECONDS);
        ConsumerRecord<String, CategoryViewEvent> receivedCategory = categoryRecords.poll(15, TimeUnit.SECONDS);
        
        assertNotNull(receivedProduct, "Should receive ProductViewEvent");
        assertNotNull(receivedCategory, "Should receive CategoryViewEvent");
        
        assertEquals(99L, receivedProduct.value().getProductId());
        assertEquals(88L, receivedCategory.value().getCategoryId());
    }
    
    @Test
    void testTripleEventsInSequence() throws InterruptedException {
        // Given
        ProductViewEvent productEvent = new ProductViewEvent(
            99L, "Product 99", "SKU-99", null, "session-999", null
        );
        
        CategoryViewEvent categoryEvent = new CategoryViewEvent(
            88L, "Category 88", null, null, 0, 1, null, "session-888", null
        );
        LocalDateTime now = LocalDateTime.now();
   		BasketViewEvent basketEvent = new BasketViewEvent(
                1L,                    // basketid
                1L,					   // userId
                "test",					// session id
                now,					//createdAt
                now							//updatedAt	
            );
        // When
   		eventProducer.sendProductViewEvent(productEvent);
   		eventProducer.sendCategoryViewEvent(categoryEvent);
   		eventProducer.sendBasketViewEvent(basketEvent);
        
        // Then
        ConsumerRecord<String, ProductViewEvent> receivedProduct = productRecords.poll(15, TimeUnit.SECONDS);
        ConsumerRecord<String, CategoryViewEvent> receivedCategory = categoryRecords.poll(15, TimeUnit.SECONDS);
        ConsumerRecord<String, BasketViewEvent> receivedBasket = basketRecords.poll(15, TimeUnit.SECONDS);
        
        assertNotNull(receivedProduct, "Should receive ProductViewEvent");
        assertNotNull(receivedCategory, "Should receive CategoryViewEvent");
        assertNotNull(receivedBasket, "Should receive BasketViewEvent");
        
        assertEquals(99L, receivedProduct.value().getProductId());
        assertEquals(88L, receivedCategory.value().getCategoryId());
        assertEquals(1L, receivedBasket.value().getBasketId());
    }
}