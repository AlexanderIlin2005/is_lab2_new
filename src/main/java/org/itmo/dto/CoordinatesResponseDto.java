package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CoordinatesResponseDto {
    private Long id;
    private Float x;
    private Integer y;
}
