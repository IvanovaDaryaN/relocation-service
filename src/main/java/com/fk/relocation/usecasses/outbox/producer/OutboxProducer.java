package com.fk.relocation.usecasses.outbox.producer;

import com.fk.relocation.OutboxDto;
import com.fk.relocation.usecasses.outbox.mapper.OutboxMapper;
import com.fk.relocation.usecasses.outbox.model.Outbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxProducer {

    @Value("${app.kafka-topics.outbox-topic.name}")
    private String outboxTopicName;

    private final OutboxMapper outboxMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOutbox(Outbox outbox) {
        log.info("Sending Outbox with id: {}", outbox.getId());

        OutboxDto outboxDto = outboxMapper.toOutboxDto(outbox);

        kafkaTemplate
                .send(outboxTopicName, outbox.getId().toString(), outboxDto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Outbox with id: {} send to partition: {}", outbox.getId(), result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send outbox with id: {}", outbox.getId(), ex);
                    }
                });
    }
}
