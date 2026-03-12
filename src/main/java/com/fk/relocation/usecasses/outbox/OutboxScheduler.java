package com.fk.relocation.usecasses.outbox;

import com.fk.relocation.usecasses.outbox.model.Outbox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxScheduler {

    private final OutboxService outboxService;

    @Value("${outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelay = 10_000)
    public void run() {
        log.info("Outbox processing started");
        boolean hasMoreMessages = true;

        while (hasMoreMessages) {
            List<Outbox> batch = outboxService.findAndLockBatch(batchSize);

            if (batch.isEmpty()) {
                hasMoreMessages = false;
            } else {
                outboxService.sendOutboxBatch(batch);

                if (batch.size() < batchSize) {
                    hasMoreMessages = false;
                }
            }
        }
        log.info("Outbox processing finished");
    }
}