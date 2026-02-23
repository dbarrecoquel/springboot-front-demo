package com.example.events.producer;


import com.example.events.config.KafkaConfig;
import com.example.events.model.BasketViewEvent;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {KafkaConfig.class, BasketEventProducer.class})
@ActiveProfiles("test")
@EmbeddedKafka(
	    partitions = 1,
	    topics = {"basket-view-events"}
	)
	class BasketEventProducerTest {
	    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    private static final String TOPIC = "basket-view-events";
    
    @Autowired
    private BasketEventProducer basketEventProducer;
    
    @MockitoBean
    private FailedEventService failedEventService;
    
    private KafkaMessageListenerContainer<String, BasketViewEvent> container;
    private BlockingQueue<ConsumerRecord<String, BasketViewEvent>> records;
    
    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test-category-group");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BasketViewEvent.class.getName());
        
        DefaultKafkaConsumerFactory<String, BasketViewEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(configs);
        
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, BasketViewEvent>) records::add);
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
    void testSendBasketViewEvent_Success() throws InterruptedException {
    	
    	LocalDateTime now = LocalDateTime.now();
        // Given public BasketViewEvent(Long basketId, Long userId, String sessionId, LocalDateTime createdAt, LocalDateTime updatedAt)
        BasketViewEvent event = new BasketViewEvent(
            1L,                    // basketid
            1L,					   // userId
            "test",					// session id
            now,					//createdAt
            now							//updatedAt	
        );
      
        // When
        basketEventProducer.sendBasketViewEvent(event);
        
        // Then
        ConsumerRecord<String,BasketViewEvent> received = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received, "Should receive a message");
        assertEquals("1", received.key(), "Key should be category ID");
        
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
    void testMultipleBasketEvents() throws InterruptedException {
    	
    	 LocalDateTime now = LocalDateTime.now();
    	 BasketViewEvent event1 = new BasketViewEvent(
                 1L,                    // basketid
                 1L,					   // userId
                 "test",					// session id
                 now,					//createdAt
                 now							//updatedAt	
             );
           
        BasketViewEvent event2= new BasketViewEvent(
                2L,                    // basketid
                1L,					   // userId
                "test",					// session id
                now,					//createdAt
                now							//updatedAt	
            );
        BasketViewEvent event3= new BasketViewEvent(
                3L,                    // basketid
                1L,					   // userId
                "test",					// session id
                now,					//createdAt
                now							//updatedAt	
            );
        // When
        basketEventProducer.sendBasketViewEvent(event1);
        basketEventProducer.sendBasketViewEvent(event2);
        basketEventProducer.sendBasketViewEvent(event3);
        
        // Then
        ConsumerRecord<String, BasketViewEvent> r1 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, BasketViewEvent> r2 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, BasketViewEvent> r3 = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        
        assertEquals(1L, r1.value().getBasketId());
        assertEquals(2L, r2.value().getBasketId());
        assertEquals(3L, r3.value().getBasketId());
    }
}