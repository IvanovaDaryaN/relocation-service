package com.fk.relocation.usecasses.handler;

import com.fk.accounting.BudgetAllocatedEvent;
import com.fk.notification.NotificationCommand;
import com.fk.profiler.CVUpdatedEvent;
import com.fk.profiler.UpdateCVCommand;
import com.fk.relocation.OutboxType;
import com.fk.relocation.RelocationStatus;
import com.fk.relocation.RelocationStatusUpdateDto;
import com.fk.relocation.persistence.repository.model.Relocation;
import com.fk.relocation.usecasses.RelocationService;
import com.fk.relocation.usecasses.mapper.RelocationMapper;
import com.fk.relocation.usecasses.outbox.OutboxService;
import com.fk.security.CheckSecurityCommand;
import com.fk.security.SecurityPassedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "${app.kafka-topics.relocation-topic.name}", containerFactory = "kafkaListenerContainerFactory")
public class RelocationEventsHandler {
    private final RelocationMapper relocationMapper;
    private final RelocationService relocationService;
    private final OutboxService outboxService;

    private static final String TYPE = "relocation";

    @KafkaHandler
    public void handleBudgetAllocatedEvent(@Payload BudgetAllocatedEvent budgetAllocatedEvent, @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        log.info("Received BudgetAllocatedEvent: key={}, event={}", messageKey, budgetAllocatedEvent);

        try {
            Long requestId = budgetAllocatedEvent.requestId();
            Relocation relocation = relocationService.getById(requestId);

            RelocationStatus currentStatus = relocation.getStatus();
            if (currentStatus == RelocationStatus.BUDGET_ALLOCATED || currentStatus == RelocationStatus.BUDGET_FAILED) {
                log.info("BudgetAllocatedEvent already processed for requestId={}, currentStatus={}. Skipping.", requestId, currentStatus);
                return;
            }

            RelocationStatus newStatus = budgetAllocatedEvent.allocated() ? RelocationStatus.BUDGET_ALLOCATED : RelocationStatus.BUDGET_FAILED;
            relocation = relocationService.updateRelocationStatus(requestId, new RelocationStatusUpdateDto(newStatus));

            if (newStatus.equals(RelocationStatus.BUDGET_ALLOCATED)) {
                CheckSecurityCommand checkSecurityCommand = relocationMapper.toCheckSecurityCommand(relocation, TYPE);
                outboxService.create(checkSecurityCommand, OutboxType.SECURITY);
            } else {
                NotificationCommand notificationCommand = relocationMapper.toNotificationCommand(relocation, TYPE);
                outboxService.create(notificationCommand, OutboxType.NOTIFICATION);
                log.warn("Budget allocation failed for requestId: {}", requestId);
            }

        } catch (Exception e) {
            log.error("Error processing BudgetAllocatedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handleSecurityPassedEvent(@Payload SecurityPassedEvent securityPassedEvent, @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        log.info("Received SecurityPassedEvent: key={}, event={}", messageKey, securityPassedEvent);

        try {
            Long requestId = securityPassedEvent.requestId();
            Relocation relocation = relocationService.getById(requestId);

            RelocationStatus currentStatus = relocation.getStatus();
            if (currentStatus == RelocationStatus.SECURITY_PASSED || currentStatus == RelocationStatus.SECURITY_FAILED) {
                log.info("SecurityCheckEvent already processed for requestId={}, currentStatus={}. Skipping.", requestId, currentStatus);
                return;
            }
            RelocationStatus newStatus = securityPassedEvent.passed() ? RelocationStatus.SECURITY_PASSED : RelocationStatus.SECURITY_FAILED;
            relocation = relocationService.updateRelocationStatus(securityPassedEvent.requestId(), new RelocationStatusUpdateDto(newStatus));

            if (newStatus.equals(RelocationStatus.SECURITY_PASSED)) {
                UpdateCVCommand updateCVCommand = relocationMapper.toUpdateCVCommand(relocation, TYPE);
                outboxService.create(updateCVCommand, OutboxType.PROFILER);
            } else {
                NotificationCommand notificationCommand = relocationMapper.toNotificationCommand(relocation, TYPE);
                outboxService.create(notificationCommand, OutboxType.NOTIFICATION);
                log.warn("Security check failed for requestId: {}", requestId);
            }

        } catch (Exception e) {
            log.error("Error processing SecurityCheckEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handleCountryUpdateEvent(@Payload CVUpdatedEvent cVUpdatedEvent, @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        log.info("Received CVUpdatedEvent: key={}, event={}", messageKey, cVUpdatedEvent);

        try {
            Long requestId = cVUpdatedEvent.requestId();
            Relocation relocation = relocationService.getById(requestId);

            RelocationStatus currentStatus = relocation.getStatus();
            if (currentStatus == RelocationStatus.FINISHED) {
                log.info("CVUpdatedEvent already processed for requestId={}, currentStatus={}. Skipping.", requestId, currentStatus);
                return;
            }
            RelocationStatus newStatus = null;
            if (cVUpdatedEvent.updated()) {
                newStatus = RelocationStatus.FINISHED;
            }
            relocation = relocationService.updateRelocationStatus(cVUpdatedEvent.requestId(), new RelocationStatusUpdateDto(newStatus));

            NotificationCommand notificationCommand = relocationMapper.toNotificationCommand(relocation, TYPE);
            outboxService.create(notificationCommand, OutboxType.NOTIFICATION);

        } catch (Exception e) {
            log.error("Error processing CVUpdatedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}
