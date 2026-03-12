package com.fk.relocation.usecasses.outbox;

import com.fk.relocation.OutboxStatus;
import com.fk.relocation.OutboxType;
import com.fk.relocation.usecasses.outbox.mapper.OutboxMapper;
import com.fk.relocation.usecasses.outbox.model.Outbox;
import com.fk.relocation.usecasses.outbox.producer.OutboxProducer;
import com.fk.relocation.usecasses.outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OutboxService {

    private final OutboxMapper outboxMapper;
    private final OutboxProducer outboxProducer;
    private final OutboxRepository outboxRepository;

    @Transactional
    public List<Outbox> findAndLockBatch(int batchSize) {
        return outboxRepository.findAndLockBatch(List.of(OutboxStatus.NEW, OutboxStatus.PENDING), PageRequest.of(0, batchSize));
    }

    @Transactional
    public void sendOutboxBatch(List<Outbox> outboxList) {
        for (Outbox outbox: outboxList) {
            try {
                outbox.setStatus(OutboxStatus.PENDING);
                outboxRepository.save(outbox);

                outboxProducer.sendOutbox(outbox);

                outbox.setStatus(OutboxStatus.PROCESSED);
                outboxRepository.save(outbox);
            } catch (Exception e) {
                log.error("Failed to send outbox with id: {}", outbox.getId(), e);
                outbox.setStatus(OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            }
        }
    }

    @Transactional
    public Outbox create(Object payload, OutboxType type) {
        Outbox outbox = outboxMapper.toOutbox(payload, type);
        return outboxRepository.save(outbox);
    }
}
