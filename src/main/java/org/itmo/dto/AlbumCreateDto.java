package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD) // Обязательно для вложенных DTO
public class AlbumCreateDto {
    private Long id;
    private String name;
    private int tracks;
    private int length;
    private Double sales;
}
