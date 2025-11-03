package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD) 
public class StudioCreateDto {
    private Long id;
    private String name;
}
