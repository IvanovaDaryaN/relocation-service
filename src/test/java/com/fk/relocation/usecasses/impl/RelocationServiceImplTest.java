package com.fk.relocation.usecasses.impl;

import com.fk.relocation.RelocationStatus;
import com.fk.relocation.RelocationStatusUpdateDto;
import com.fk.relocation.persistence.repository.RelocationRepository;
import com.fk.relocation.persistence.repository.model.Relocation;
import com.fk.relocation.usecasses.mapper.RelocationMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelocationServiceImplTest {

    @Mock
    private RelocationRepository repository;

    @Mock
    private RelocationMapper relocationMapper;

    @InjectMocks
    private RelocationServiceImpl relocationService;

    @ParameterizedTest
    @EnumSource(value = RelocationStatus.class, names = {"CREATED"})
    void updateRelocationStatus_shouldUpdateStatusSuccessfully(RelocationStatus newStatus) {
        // Arrange
        Long requestId = 1L;
        RelocationStatusUpdateDto dto = new RelocationStatusUpdateDto(newStatus);

        Relocation existingRelocation = new Relocation();
        existingRelocation.setId(requestId);
        existingRelocation.setName("John");
        existingRelocation.setSurname("Doe");
        existingRelocation.setStatus(RelocationStatus.CREATED);

        Relocation updatedRelocation = new Relocation();
        updatedRelocation.setId(requestId);
        updatedRelocation.setName("John");
        updatedRelocation.setSurname("Doe");
        updatedRelocation.setStatus(newStatus);

        when(repository.findById(requestId)).thenReturn(Optional.of(existingRelocation));
        when(repository.save(any(Relocation.class))).thenReturn(updatedRelocation);

        // Act
        Relocation result = relocationService.updateRelocationStatus(requestId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(requestId, result.getId());

        verify(repository).findById(requestId);
        verify(repository).save(any(Relocation.class));
    }
}