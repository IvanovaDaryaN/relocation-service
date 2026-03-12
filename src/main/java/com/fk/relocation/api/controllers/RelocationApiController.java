package com.fk.relocation.api.controllers;

import com.fk.relocation.CreateRelocationRequestDto;
import com.fk.relocation.RelocationDto;
import com.fk.relocation.RelocationStatusUpdateDto;
import com.fk.relocation.usecasses.RelocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class RelocationApiController implements RelocationApi {

    private final RelocationService relocationService;

    @Override
    public ResponseEntity<RelocationDto> createRelocationRequest(CreateRelocationRequestDto request) {
        RelocationDto responseDto = relocationService.createRelocationRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Override
    public ResponseEntity<RelocationDto> getDtoRequestById(Long requestId) {
        RelocationDto dto = relocationService.getByRequestId(requestId);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<RelocationDto> updateStatus(Long requestId, RelocationStatusUpdateDto dto) {
        RelocationDto relocationDto = relocationService.updateStatus(requestId, dto);
        return ResponseEntity.ok(relocationDto);
    }
}
