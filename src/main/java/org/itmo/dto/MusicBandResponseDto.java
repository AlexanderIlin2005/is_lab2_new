package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.itmo.model.MusicGenre;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class MusicBandResponseDto {
    private Long id;
    private String name;
    private CoordinatesResponseDto coordinates;
    private ZonedDateTime creationDate;
    private MusicGenre genre;
    private long numberOfParticipants;
    private Long singleCount;
    private String description;
    private AlbumResponseDto bestAlbum;
    private int albumCount;
    private ZonedDateTime establishmentDate;
    private StudioResponseDto studio;
}
