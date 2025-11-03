package org.itmo.dto;

import lombok.Value;


@Value
public class ImportResultDto {
    
    int imported;

    
    String message;

    
    boolean success;
}