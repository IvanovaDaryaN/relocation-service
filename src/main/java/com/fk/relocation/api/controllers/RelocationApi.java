package com.fk.relocation.api.controllers;

import com.fk.relocation.CreateRelocationRequestDto;
import com.fk.relocation.RelocationDto;
import com.fk.relocation.RelocationStatusUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Tag(name = "Relocation Controller")
@RequestMapping("/api/v1/relocation")
public interface RelocationApi {

    @Operation(summary = "Create relocation request")
    @ApiResponse(
            responseCode = "201",
            description = "CREATED",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RelocationDto.class)))
    @ApiResponse(responseCode = "400", description = "BAD REQUEST - Invalid input data")
    @ApiResponse(responseCode = "401", description = "UNAUTHORIZED - no token provided")
    @PostMapping(
            value = "/request",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<RelocationDto> createRelocationRequest(@Valid @RequestBody CreateRelocationRequestDto request);

    @Operation(summary = "Get relocation by requestId")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RelocationDto.class)))
    @ApiResponse(responseCode = "401", description = "UNAUTHORIZED - no token provided")
    @ApiResponse(responseCode = "404", description = "RELOCATION NOT FOUND")
    @GetMapping(value = "/request/{requestId}", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<RelocationDto> getDtoRequestById(@PathVariable("requestId") Long requestId);

    @Operation(summary = "Update relocation status")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RelocationDto.class)))
    @ApiResponse(responseCode = "400", description = "BAD REQUEST - incorrect data")
    @ApiResponse(responseCode = "401", description = "UNAUTHORIZED - no token provided")
    @ApiResponse(responseCode = "404", description = "RELOCATION NOT FOUND")
    @PatchMapping(value = "/request/{requestId}", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<RelocationDto> updateStatus(@PathVariable("requestId") Long requestId, @Valid @RequestBody RelocationStatusUpdateDto dto);
}
