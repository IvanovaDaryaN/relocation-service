package com.fk.relocation.usecasses.outbox.mapper;

import com.fk.relocation.OutboxDto;
import com.fk.relocation.OutboxStatus;
import com.fk.relocation.OutboxType;
import com.fk.relocation.usecasses.outbox.model.Outbox;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxMapper {

    private final ObjectMapper objectMapper;

    public Outbox toOutbox(Object payload, OutboxType type) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            return Outbox.builder()
                    .payload(jsonPayload)
                    .type(type)
                    .status(OutboxStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }

    public OutboxDto toOutboxDto(Outbox outbox) {
        return new OutboxDto(
                outbox.getId().toString(),
                outbox.getCreatedAt(),
                outbox.getPayload(),
                outbox.getType()
        );
    }
}