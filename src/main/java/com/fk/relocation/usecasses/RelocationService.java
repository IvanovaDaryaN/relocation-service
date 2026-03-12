package com.fk.relocation.usecasses;

import com.fk.relocation.CreateRelocationRequestDto;
import com.fk.relocation.RelocationDto;
import com.fk.relocation.RelocationStatusUpdateDto;
import com.fk.relocation.persistence.repository.model.Relocation;

public interface RelocationService {

    RelocationDto createRelocationRequest(CreateRelocationRequestDto dto);

    RelocationDto getByRequestId(Long id);

    Relocation getById(Long id);

    RelocationDto updateStatus(Long requestId, RelocationStatusUpdateDto dto);

    Relocation updateRelocationStatus(Long requestId, RelocationStatusUpdateDto dto);
}
