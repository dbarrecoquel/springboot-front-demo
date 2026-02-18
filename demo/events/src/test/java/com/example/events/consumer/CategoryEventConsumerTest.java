package com.example.events.consumer;

import com.example.events.config.KafkaConfig;
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

@SpringBootTest(classes = {KafkaConfig.class, CategoryEventConsumer.class})
@ActiveProfiles("test")
@EmbeddedKafka(
	    partitions = 1,
	    topics = {"category-view-events"}
	)
class CategoryEventConsumerTest {
    
    private static final String TOPIC = "category-view-events";
    
    @MockitoSpyBean
    private CategoryEventConsumer categoryEventConsumer;
    
    private KafkaTemplate<String, CategoryViewEvent> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @BeforeEach
    void setUp() {
        // Configuration du producer de test
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        DefaultKafkaProducerFactory<String, CategoryViewEvent> producerFactory = 
            new DefaultKafkaProducerFactory<>(configs);
        
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
        
        // Reset le spy avant chaque test
        reset(categoryEventConsumer);
    }
    
    @AfterEach
    void tearDown() {
        if (kafkaTemplate != null) {
            kafkaTemplate.destroy();
        }
    }
    
    @Test
    void testConsumeCategoryViewEvent_RootCategory() {
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
            "test-session-123",    // sessionId
            null                   // userEmail
        );
        event.setBreadcrumbPath("Électronique");
        event.setIpAddress("192.168.1.1");
        
        // When
        kafkaTemplate.send(TOPIC, "1", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
            });
        
        ArgumentCaptor<CategoryViewEvent> eventCaptor = ArgumentCaptor.forClass(CategoryViewEvent.class);
        verify(categoryEventConsumer).consume(eventCaptor.capture());
        
        CategoryViewEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(1L, capturedEvent.getCategoryId());
        assertEquals("Électronique", capturedEvent.getCategoryName());
        assertNull(capturedEvent.getParentCategoryId());
        assertNull(capturedEvent.getParentCategoryName());
        assertEquals(0, capturedEvent.getDepthLevel());
        assertEquals(3, capturedEvent.getSubcategoriesCount());
        assertEquals(15, capturedEvent.getProductsCount());
        assertEquals("Électronique", capturedEvent.getBreadcrumbPath());
        assertNotNull(capturedEvent.getEventId());
        assertNotNull(capturedEvent.getTimestamp());
    }
    
    @Test
    void testConsumeCategoryViewEvent_SubCategory() {
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
        event.setUserAgent("Mozilla/5.0");
        
        // When
        kafkaTemplate.send(TOPIC, "3", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
            });
        
        ArgumentCaptor<CategoryViewEvent> eventCaptor = ArgumentCaptor.forClass(CategoryViewEvent.class);
        verify(categoryEventConsumer).consume(eventCaptor.capture());
        
        CategoryViewEvent capturedEvent = eventCaptor.getValue();
        assertEquals(3L, capturedEvent.getCategoryId());
        assertEquals("Smartphones", capturedEvent.getCategoryName());
        assertEquals(1L, capturedEvent.getParentCategoryId());
        assertEquals("Électronique", capturedEvent.getParentCategoryName());
        assertEquals(1, capturedEvent.getDepthLevel());
        assertEquals(2, capturedEvent.getSubcategoriesCount());
        assertEquals(8, capturedEvent.getProductsCount());
        assertEquals("Électronique > Smartphones", capturedEvent.getBreadcrumbPath());
        assertEquals(42L, capturedEvent.getUserId());
        assertEquals("user@example.com", capturedEvent.getUserEmail());
    }
    
    @Test
    void testConsumeCategoryViewEvent_DeepHierarchy() {
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
        kafkaTemplate.send(TOPIC, "5", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
            });
        
        ArgumentCaptor<CategoryViewEvent> eventCaptor = ArgumentCaptor.forClass(CategoryViewEvent.class);
        verify(categoryEventConsumer).consume(eventCaptor.capture());
        
        CategoryViewEvent capturedEvent = eventCaptor.getValue();
        assertEquals(5L, capturedEvent.getCategoryId());
        assertEquals(2, capturedEvent.getDepthLevel());
        assertEquals("Électronique > Smartphones > iPhone", capturedEvent.getBreadcrumbPath());
        assertEquals(0, capturedEvent.getSubcategoriesCount(), "Leaf category should have 0 subcategories");
    }
    
    @Test
    void testConsumeCategoryViewEvent_AnonymousUser() {
        // Given
        CategoryViewEvent event = new CategoryViewEvent(
            2L,
            "Vêtements",
            null,
            null,
            0,
            5,
            20,
            null,  // userId null
            "anonymous-session",
            null   // userEmail null
        );
        event.setBreadcrumbPath("Vêtements");
        
        // When
        kafkaTemplate.send(TOPIC, "2", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
            });
        
        ArgumentCaptor<CategoryViewEvent> eventCaptor = ArgumentCaptor.forClass(CategoryViewEvent.class);
        verify(categoryEventConsumer).consume(eventCaptor.capture());
        
        CategoryViewEvent capturedEvent = eventCaptor.getValue();
        assertNull(capturedEvent.getUserId());
        assertNull(capturedEvent.getUserEmail());
        assertEquals("anonymous-session", capturedEvent.getSessionId());
    }
    
    @Test
    void testConsumeMultipleCategoryViewEvents() {
        // Given
        int numberOfEvents = 5;
        
        // When
        for (int i = 1; i <= numberOfEvents; i++) {
            CategoryViewEvent event = new CategoryViewEvent(
                (long) i,
                "Category " + i,
                null,
                null,
                0,
                i,
                i * 5,
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
                verify(categoryEventConsumer, times(numberOfEvents)).consume(any(CategoryViewEvent.class));
            });
    }
    
    @Test
    void testConsumeCategoryViewEvent_EmptyCategory() {
        // Given - Catégorie sans sous-catégories ni produits
        CategoryViewEvent event = new CategoryViewEvent(
            10L,
            "Nouvelle Catégorie",
            null,
            null,
            0,
            0,  // Pas de sous-catégories
            0,  // Pas de produits
            null,
            "test-session",
            null
        );
        event.setBreadcrumbPath("Nouvelle Catégorie");
        
        // When
        kafkaTemplate.send(TOPIC, "10", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
            });
        
        ArgumentCaptor<CategoryViewEvent> eventCaptor = ArgumentCaptor.forClass(CategoryViewEvent.class);
        verify(categoryEventConsumer).consume(eventCaptor.capture());
        
        CategoryViewEvent capturedEvent = eventCaptor.getValue();
        assertEquals(0, capturedEvent.getSubcategoriesCount());
        assertEquals(0, capturedEvent.getProductsCount());
    }
    
    @Test
    void testConsumeCategoryViewEvent_AllFieldsPopulated() {
        // Given
        CategoryViewEvent event = new CategoryViewEvent(
            99L,
            "Complete Category",
            50L,
            "Parent Category",
            3,
            7,
            42,
            999L,
            "complete-session-id",
            "complete@example.com"
        );
        event.setBreadcrumbPath("Root > Parent > Child > Complete Category");
        event.setIpAddress("10.0.0.1");
        event.setUserAgent("CompleteAgent/1.0");
        
        // When
        kafkaTemplate.send(TOPIC, "99", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
            });
        
        ArgumentCaptor<CategoryViewEvent> eventCaptor = ArgumentCaptor.forClass(CategoryViewEvent.class);
        verify(categoryEventConsumer).consume(eventCaptor.capture());
        
        CategoryViewEvent capturedEvent = eventCaptor.getValue();
        
        // Vérifier tous les champs
        assertNotNull(capturedEvent.getEventId(), "Event ID should not be null");
        assertEquals(99L, capturedEvent.getCategoryId(), "Category ID mismatch");
        assertEquals("Complete Category", capturedEvent.getCategoryName(), "Category name mismatch");
        assertEquals(50L, capturedEvent.getParentCategoryId(), "Parent category ID mismatch");
        assertEquals("Parent Category", capturedEvent.getParentCategoryName(), "Parent category name mismatch");
        assertEquals(3, capturedEvent.getDepthLevel(), "Depth level mismatch");
        assertEquals(7, capturedEvent.getSubcategoriesCount(), "Subcategories count mismatch");
        assertEquals(42, capturedEvent.getProductsCount(), "Products count mismatch");
        assertEquals(999L, capturedEvent.getUserId(), "User ID mismatch");
        assertEquals("complete-session-id", capturedEvent.getSessionId(), "Session ID mismatch");
        assertEquals("complete@example.com", capturedEvent.getUserEmail(), "User email mismatch");
        assertEquals("Root > Parent > Child > Complete Category", capturedEvent.getBreadcrumbPath(), "Breadcrumb path mismatch");
        assertEquals("10.0.0.1", capturedEvent.getIpAddress(), "IP address mismatch");
        assertEquals("CompleteAgent/1.0", capturedEvent.getUserAgent(), "User agent mismatch");
        assertNotNull(capturedEvent.getTimestamp(), "Timestamp should not be null");
    }
    
    @Test
    void testConsumeCategoryViewEvent_VerifyTimestamp() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        CategoryViewEvent event = new CategoryViewEvent(
            1L, "Test", null, null, 0, 1, 5, null, "session", null
        );
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        // When
        kafkaTemplate.send(TOPIC, "1", event);
        
        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
            });
        
        ArgumentCaptor<CategoryViewEvent> eventCaptor = ArgumentCaptor.forClass(CategoryViewEvent.class);
        verify(categoryEventConsumer).consume(eventCaptor.capture());
        
        LocalDateTime timestamp = eventCaptor.getValue().getTimestamp();
        assertNotNull(timestamp);
        assertTrue(timestamp.isAfter(before) || timestamp.isEqual(before));
        assertTrue(timestamp.isBefore(after) || timestamp.isEqual(after));
    }
    
    @Test
    void testConsumerIsInitialized() {
        // Given & When
        // Le consumer est automatiquement initialisé par Spring
        
        // Then
        assertNotNull(categoryEventConsumer, "Consumer should be initialized");
    }
    
    @Test
    void testConsumeCategoryViewEvent_NoExceptionThrown() {
        // Given
        CategoryViewEvent event = new CategoryViewEvent(
            1L, "Safe Category", null, null, 0, 2, 10, null, "safe-session", null
        );
        
        // When & Then - Ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            kafkaTemplate.send(TOPIC, "1", event);
            
            await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(categoryEventConsumer, times(1)).consume(any(CategoryViewEvent.class));
                });
        });
    }
}