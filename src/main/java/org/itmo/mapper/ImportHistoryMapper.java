

package org.itmo.mapper;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.model.ImportHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImportHistoryMapper {

    
    @Mapping(source = "launchedBy.username", target = "launchedByUsername")
    ImportHistoryResponseDto toResponseDto(ImportHistory history);
}