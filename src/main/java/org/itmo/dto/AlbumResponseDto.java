package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AlbumResponseDto {
    private Long id;
    private String name;
    private int tracks;
    private int length;
    private Double sales;
}
