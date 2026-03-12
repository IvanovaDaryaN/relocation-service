package com.fk.relocation.usecasses.producer;

import com.fk.accounting.AllocateBudgetCommand;
import com.fk.notification.NotificationCommand;
import com.fk.security.CheckSecurityCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RelocationProducer {

    @Value("${app.kafka-topics.accounting-topic.name}")
    private String accountingTopicName;

    @Value("${app.kafka-topics.security-topic.name}")
    private String securityTopicName;

    @Value("${app.kafka-topics.notification-topic.name}")
    private String notificationTopicName;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAllocateBudgetCommand(AllocateBudgetCommand allocateBudgetCommand) {
        log.info("Sending AllocateBudgetCommand with dto: {}", allocateBudgetCommand);

        kafkaTemplate
                .send(accountingTopicName, allocateBudgetCommand.requestId().toString(), allocateBudgetCommand)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("AllocateBudgetCommand send to partition: {}", result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send AllocateBudgetCommand", ex);
                    }
                });
    }

    public void sendCheckSecurityCommand(CheckSecurityCommand checkSecurityCommand) {
        log.info("Sending CheckSecurityCommand with dto: {}", checkSecurityCommand);

        kafkaTemplate
                .send(securityTopicName, checkSecurityCommand.requestId().toString(), checkSecurityCommand)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("CheckSecurityCommand send to partition: {}", result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send CheckSecurityCommand", ex);
                    }
                });
    }

    public void sendNotificationCommand(NotificationCommand notificationCommand) {
        log.info("Sending NotificationCommand with dto: {}", notificationCommand);

        kafkaTemplate
                .send(notificationTopicName, notificationCommand.requestId().toString(), notificationCommand)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("NotificationCommand send to partition: {}", result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send NotificationCommand", ex);
                    }
                });
        log.info("NotificationCommand sent with dto: {}", notificationCommand);
    }
}
