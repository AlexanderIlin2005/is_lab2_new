package org.itmo.mapper;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.model.ImportHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImportHistoryMapper {

    /**
     * Маппинг поля launchedBy.username в launchedByUsername в DTO.
     */
    @Mapping(source = "launchedBy.username", target = "launchedByUsername")
    ImportHistoryResponseDto toResponseDto(ImportHistory entity);

    List<ImportHistoryResponseDto> toResponseDto(List<ImportHistory> entities);
}