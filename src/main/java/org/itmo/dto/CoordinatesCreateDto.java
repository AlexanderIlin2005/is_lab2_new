package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD) // Обязательно для вложенных DTO
public class CoordinatesCreateDto {
    private Long id;
    private Float x;
    private Integer y;
}
