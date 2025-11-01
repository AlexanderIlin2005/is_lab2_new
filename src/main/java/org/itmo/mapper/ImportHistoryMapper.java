// src/main/java/org.itmo.mapper/ImportHistoryMapper.java

package org.itmo.mapper;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.model.ImportHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImportHistoryMapper {

    /**
     * Маппинг сущности ImportHistory в DTO.
     * Мы вручную мапим поле launchedBy (сущность User) на launchedByUsername (String).
     */
    @Mapping(source = "launchedBy.username", target = "launchedByUsername")
    ImportHistoryResponseDto toResponseDto(ImportHistory history);
}