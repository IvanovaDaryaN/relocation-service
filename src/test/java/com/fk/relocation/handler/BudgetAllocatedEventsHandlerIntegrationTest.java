package com.fk.relocation.handler;

import com.fk.accounting.BudgetAllocatedEvent;
import com.fk.relocation.RelocationStatus;
import com.fk.relocation.RelocationStatusUpdateDto;
import com.fk.relocation.annotations.KafkaIntegrationTest;
import com.fk.relocation.persistence.repository.RelocationRepository;
import com.fk.relocation.persistence.repository.model.Relocation;
import com.fk.relocation.usecasses.RelocationService;
import com.fk.relocation.usecasses.handler.RelocationEventsHandler;
import com.fk.relocation.usecasses.outbox.repository.OutboxRepository;
import com.fk.relocation.usecasses.producer.RelocationProducer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Disabled
@KafkaIntegrationTest
public class BudgetAllocatedEventsHandlerIntegrationTest {

    private static final int MESSAGE_TIMEOUT_MS = 5000;
    private static final int POLLING_INTERVAL_MS = 500;

    @Value("${app.kafka-topics.relocation-topic.name}")
    private String relocationTopic;

    @Value("${app.kafka-topics.dlt-topic.name}")
    private String dltTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @MockitoSpyBean
    private RelocationEventsHandler relocationEventsHandler;

    @MockitoBean
    private RelocationService relocationService;

    @MockitoBean
    private RelocationRepository relocationRepository;

    @MockitoBean
    private JpaTransactionManager transactionManager;

    @MockitoBean
    private RelocationProducer relocationProducer;

    @Test
    void relocationEventsHandlerShouldBeCalledWhenBudgetAllocatedEventIsSentToRelocationTopic() {
        //given
        Long eventId = 999L;
        Long relocationId = 4L;
        final boolean ALLOCATED = true;

        var event = new BudgetAllocatedEvent(eventId, relocationId, true);
        var messageKey = String.valueOf(eventId);

        Relocation mockRelocation = mock(Relocation.class);
        when(mockRelocation.getId()).thenReturn(relocationId);
        when(mockRelocation.getStatus()).thenReturn(RelocationStatus.CREATED);

        when(relocationService.getById(relocationId))
                .thenReturn(mockRelocation);

        when(relocationService.updateRelocationStatus(eq(relocationId), any(RelocationStatusUpdateDto.class)))
                .thenReturn(mockRelocation);

        doNothing().when(relocationProducer).sendCheckSecurityCommand(any());
        doNothing().when(relocationProducer).sendNotificationCommand(any());

        //when
        kafkaTemplate.executeInTransaction(
                operations -> operations.send(relocationTopic, messageKey, event)
        );

        //then
        checkIfMessageNotInDltTopic(dltTopic);

        Awaitility.await()
                .atMost(Duration.ofMillis(MESSAGE_TIMEOUT_MS))
                .pollInterval(Duration.ofMillis(POLLING_INTERVAL_MS))
                .untilAsserted(() -> {
                    verify(relocationEventsHandler, atLeastOnce())
                            .handleBudgetAllocatedEvent(eq(event), eq(messageKey));
                    verify(relocationService, atLeastOnce()).getById(relocationId);
                    if (ALLOCATED) {
                        verify(relocationService, atLeastOnce())
                                .updateRelocationStatus(eq(relocationId), any(RelocationStatusUpdateDto.class));
                        verify(relocationProducer, atLeastOnce()).sendCheckSecurityCommand(any());
                    }
                });
    }

    private void checkIfMessageNotInDltTopic(String dltTopic) {
        try (var consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of(dltTopic));
            var records = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(MESSAGE_TIMEOUT_MS));
            assertThat(records.count()).isEqualTo(0);
        }
    }
}
