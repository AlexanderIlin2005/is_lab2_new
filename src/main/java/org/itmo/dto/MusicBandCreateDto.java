package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.itmo.model.MusicGenre;

import java.time.ZonedDateTime;


import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;


import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Data
@NoArgsConstructor
@XmlRootElement(name = "musicBand") 
@XmlAccessorType(XmlAccessType.FIELD) 
public class MusicBandCreateDto {
    private String name;
    private CoordinatesCreateDto coordinates;
    private MusicGenre genre;
    private long numberOfParticipants;
    private Long singleCount;
    private String description;
    private AlbumCreateDto bestAlbum;
    private int albumCount;

    
    @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
    private ZonedDateTime establishmentDate;
    private StudioCreateDto studio;
}
