package com.example.events.config;

import com.example.events.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // ==================== PRODUCER GÉNÉRIQUE ====================

    @Bean
    public ProducerFactory<String, Object> genericProducerFactory(ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        return new DefaultKafkaProducerFactory<>(
            config,
            new StringSerializer(),
            new JsonSerializer<>(objectMapper)
        );
    }

    @Bean("genericKafkaTemplate")
    public KafkaTemplate<String, Object> genericKafkaTemplate(ObjectMapper objectMapper) {
        return new KafkaTemplate<>(genericProducerFactory(objectMapper));
    }

    // ==================== CONSUMER FACTORY HELPER ====================

    private <T> ConsumerFactory<String, T> buildConsumerFactory(
            ObjectMapper objectMapper, String groupId, Class<T> targetType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType, objectMapper, false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(deserializer)
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> buildContainerFactory(
            ConsumerFactory<String, T> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setAutoStartup(true);
        return factory;
    }

    // ==================== CONSUMERS ====================

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductViewEvent>
            kafkaListenerContainerFactory(ObjectMapper objectMapper) {
        return buildContainerFactory(
            buildConsumerFactory(objectMapper, "product-events-group", ProductViewEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CategoryViewEvent>
            categoryKafkaListenerContainerFactory(ObjectMapper objectMapper) {
        return buildContainerFactory(
            buildConsumerFactory(objectMapper, "category-events-group", CategoryViewEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BasketViewEvent>
            basketKafkaListenerContainerFactory(ObjectMapper objectMapper) {
        return buildContainerFactory(
            buildConsumerFactory(objectMapper, "basket-events-group", BasketViewEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AddToBasketEvent>
            addToBasketKafkaListenerContainerFactory(ObjectMapper objectMapper) {
        return buildContainerFactory(
            buildConsumerFactory(objectMapper, "add-to-basket-events-group", AddToBasketEvent.class));
    }

    // ==================== CONSUMERS PANIER ABANDONNÉ ====================

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BasketCreatedEvent>
            basketCreatedKafkaListenerContainerFactory(ObjectMapper objectMapper) {
        return buildContainerFactory(
            buildConsumerFactory(objectMapper, "basket-reconciliation-group", BasketCreatedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BasketConvertedEvent>
            basketConvertedKafkaListenerContainerFactory(ObjectMapper objectMapper) {
        return buildContainerFactory(
            buildConsumerFactory(objectMapper, "basket-reconciliation-group", BasketConvertedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BasketAbandonedEvent>
            basketAbandonedKafkaListenerContainerFactory(ObjectMapper objectMapper) {
        return buildContainerFactory(
            buildConsumerFactory(objectMapper, "basket-abandoned-group", BasketAbandonedEvent.class));
    }
}