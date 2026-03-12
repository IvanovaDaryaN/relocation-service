package com.fk.relocation.producer;

import com.fk.accounting.AllocateBudgetCommand;
import com.fk.relocation.annotations.KafkaIntegrationTest;
import com.fk.relocation.usecasses.RelocationService;
import com.fk.relocation.usecasses.outbox.repository.OutboxRepository;
import com.fk.relocation.usecasses.producer.RelocationProducer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@KafkaIntegrationTest
public class RelocationProducerIntegrationTest {

    private static final int MESSAGE_TIMEOUT_MS = 5000;

    @Value("${app.kafka-topics.accounting-topic.name}")
    private String accountingTopic;

    @Autowired
    private RelocationProducer relocationProducer;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private RelocationService relocationService;

    @MockBean
    private OutboxRepository outboxRepository;

    @Test
    void allocateBudgetCommandIsSentToAccountingTopic() {
        // given
        var allocateBudgetCommand = new AllocateBudgetCommand(
                222L,
                "test budget allocation",
                new BigDecimal("50000")
        );

        // when
        kafkaTemplate.executeInTransaction(operations -> {
            operations.send(accountingTopic, allocateBudgetCommand);
            return null;
        });

        // then
        try (var consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of(accountingTopic));
            var record = KafkaTestUtils.getSingleRecord(
                    consumer,
                    accountingTopic,
                    Duration.ofMillis(MESSAGE_TIMEOUT_MS)
            );

            assertThat(record).isNotNull();
            assertThat(record.value()).isEqualTo(allocateBudgetCommand);
        }
    }
}