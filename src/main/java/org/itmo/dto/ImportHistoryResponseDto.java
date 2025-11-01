package org.itmo.dto;

import org.itmo.model.enums.ImportStatus;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class ImportHistoryResponseDto {
    Long id;
    String launchedByUsername;
    ZonedDateTime startTime;
    ZonedDateTime endTime;
    ImportStatus status;
    Integer addedCount;
    String errorDetails;
}