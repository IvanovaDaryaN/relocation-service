package com.fk.relocation.usecasses.mapper;

import com.fk.accounting.AllocateBudgetCommand;
import com.fk.notification.NotificationCommand;
import com.fk.profiler.UpdateCVCommand;
import com.fk.relocation.CreateRelocationRequestDto;
import com.fk.relocation.RelocationDto;
import com.fk.relocation.persistence.repository.model.Relocation;
import com.fk.security.CheckSecurityCommand;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public interface RelocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Relocation toEntity(CreateRelocationRequestDto dto);

    RelocationDto toDto(Relocation relocation);

    @Mapping(target = "requestId", source = "relocation.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "budget", source = "relocation.budget")
    AllocateBudgetCommand toAllocateBudgetCommand(Relocation relocation, String type);

    @Mapping(target = "requestId", source = "relocation.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "relocation.name")
    @Mapping(target = "surname", source = "relocation.surname")
    @Mapping(target = "cvUuid", source = "relocation.cvUuid")
    CheckSecurityCommand toCheckSecurityCommand(Relocation relocation, String type);

    @Mapping(target = "requestId", source = "relocation.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "cvUuid", source = "relocation.cvUuid")
    @Mapping(target = "countryId", source = "relocation.countryId")
    UpdateCVCommand toUpdateCVCommand(Relocation relocation, String type);

    @Mapping(target = "requestId", source = "relocation.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "relocation.status")
    NotificationCommand toNotificationCommand(Relocation relocation, String type);
}
