package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.itmo.model.MusicGenre;

import java.time.ZonedDateTime;

// НОВЫЕ АННОТАЦИИ ДЛЯ JAXB
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;

// НОВЫЕ ИМПОРТЫ
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Data
@NoArgsConstructor
@XmlRootElement(name = "musicBand") // Имя корневого элемента в списке
@XmlAccessorType(XmlAccessType.FIELD) // Маппинг по полям
public class MusicBandCreateDto {
    private String name;
    private CoordinatesCreateDto coordinates;
    private MusicGenre genre;
    private long numberOfParticipants;
    private Long singleCount;
    private String description;
    private AlbumCreateDto bestAlbum;
    private int albumCount;

    // ПРИМЕНЯЕМ АДАПТЕР К ПОЛЮ ДАТЫ
    @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
    private ZonedDateTime establishmentDate;
    private StudioCreateDto studio;
}
