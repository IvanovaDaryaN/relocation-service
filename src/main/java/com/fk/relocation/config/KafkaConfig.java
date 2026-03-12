package com.fk.relocation.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    private static final int BACKOFF_INTERVAL = 500;
    private static final int BACKOFF_ATTEMPTS = 5;
    private static final String COMMON_DLT_TOPIC = "common-dead-letter-topic";

    @Bean
    ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        var consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        consumerProperties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    log.error("Failed to process message. Sending to DLT. Topic: {}, Partition: {}, Offset: {}, Error: {}",
                            record.topic(), record.partition(), record.offset(),
                            exception != null ? exception.getMessage() : "Unknown error");
                    return new TopicPartition(COMMON_DLT_TOPIC, record.partition());
                });

        var errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(BACKOFF_INTERVAL, BACKOFF_ATTEMPTS));

        errorHandler.setCommitRecovered(true);
        errorHandler.setAckAfterHandle(false);

        errorHandler.addNotRetryableExceptions(DeserializationException.class, MessageConversionException.class,
                MethodArgumentNotValidException.class, SerializationException.class);
        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(KafkaProperties kafkaProperties,
                                                                                          ConsumerFactory<String, Object> consumerFactory,
                                                                                          DefaultErrorHandler errorHandler) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> producerProps = new HashMap<>(kafkaProperties.buildProducerProperties());
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", JsonSerializer.class);
        var producerFactory = new DefaultKafkaProducerFactory<String, Object>(producerProps);
        producerFactory.setTransactionIdPrefix(kafkaProperties.getProducer().getTransactionIdPrefix());
        return producerFactory;
    }

    @Bean
    KafkaTransactionManager<String, Object> kafkaTransactionManager(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        var kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setObservationEnabled(true);
        return kafkaTemplate;
    }

    @Bean
    public NewTopic relocationTopic(RelocationTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        String.valueOf(topicProperties.getMinInSyncReplicas())
                ))
                .build();
    }

    @Bean
    public NewTopic accountingTopic(AccountingTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        String.valueOf(topicProperties.getMinInSyncReplicas())
                ))
                .build();
    }

    @Bean
    public NewTopic securityTopic(SecurityTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        String.valueOf(topicProperties.getMinInSyncReplicas())
                ))
                .build();
    }

    @Bean
    public NewTopic notificationTopic(NotificationTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        String.valueOf(topicProperties.getMinInSyncReplicas())
                ))
                .build();
    }

    @Bean
    public NewTopic profilerTopic(ProfilerTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        String.valueOf(topicProperties.getMinInSyncReplicas())
                ))
                .build();
    }

    @Bean
    public NewTopic outboxTopic(OutboxTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        String.valueOf(topicProperties.getMinInSyncReplicas())
                ))
                .build();
    }

    @Bean
    public NewTopic dltTopic(RelocationTopicProperties topicProperties) {
        return TopicBuilder
                .name(COMMON_DLT_TOPIC)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        String.valueOf(topicProperties.getMinInSyncReplicas())
                ))
                .build();
    }

}
