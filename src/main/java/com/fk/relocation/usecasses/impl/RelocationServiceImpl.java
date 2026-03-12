package com.fk.relocation.usecasses.impl;

import com.fk.accounting.AllocateBudgetCommand;
import com.fk.relocation.*;
import com.fk.relocation.api.exception.RelocationNotFoundException;
import com.fk.relocation.persistence.repository.RelocationRepository;
import com.fk.relocation.persistence.repository.model.Relocation;
import com.fk.relocation.usecasses.RelocationService;
import com.fk.relocation.usecasses.mapper.RelocationMapper;
import com.fk.relocation.usecasses.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelocationServiceImpl implements RelocationService {

    private final RelocationRepository repository;
    private final RelocationMapper relocationMapper;
    private final OutboxService outboxService;
    private static final String TYPE = "relocation";

    @Override
    @Transactional
    public RelocationDto createRelocationRequest(CreateRelocationRequestDto createRelocationRequestDto) {
        Relocation relocation = createRelocation(createRelocationRequestDto);

        AllocateBudgetCommand allocateBudgetCommand = relocationMapper.toAllocateBudgetCommand(relocation, TYPE);
        outboxService.create(allocateBudgetCommand, OutboxType.ACCOUNTING);

        return relocationMapper.toDto(relocation);
    }

    public Relocation createRelocation(CreateRelocationRequestDto dto) {
        Relocation relocation = relocationMapper.toEntity(dto);
        relocation.setStatus(RelocationStatus.CREATED);
        Relocation savedRelocation = repository.save(relocation);
        log.info("Created relocation with id: {}", savedRelocation.getId());
        return savedRelocation;
    }

    @Override
    @Transactional(readOnly = true)
    public RelocationDto getByRequestId(Long requestId) {
        Relocation relocation = repository
                .findById(requestId)
                .orElseThrow(() -> new RelocationNotFoundException("Relocation not found with id: " + requestId));
        return relocationMapper.toDto(relocation);
    }

    @Override
    @Transactional(readOnly = true)
    public Relocation getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RelocationNotFoundException("Relocation not found with id: " + id));
    }

    @Override
    @Transactional
    public RelocationDto updateStatus(Long requestId, RelocationStatusUpdateDto dto) {
        return relocationMapper.toDto(updateRelocationStatus(requestId, dto));
    }

    @Transactional
    public Relocation updateRelocationStatus(Long requestId, RelocationStatusUpdateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (dto.relocationStatus() == null) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        Relocation relocation = repository.findById(requestId).orElseThrow(() -> new RelocationNotFoundException("Relocation not found with id: " + requestId));

        relocation.setStatus(dto.relocationStatus());
        Relocation updatedRelocation = repository.save(relocation);

        log.info("Status updated for request {}: {}", requestId, dto.relocationStatus());
        return updatedRelocation;
    }
}
