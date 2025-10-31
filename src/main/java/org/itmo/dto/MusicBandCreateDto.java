package org.itmo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.itmo.model.MusicGenre;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class MusicBandCreateDto {
    private String name;
    private CoordinatesCreateDto coordinates;
    private MusicGenre genre;
    private long numberOfParticipants;
    private Long singleCount;
    private String description;
    private AlbumCreateDto bestAlbum;
    private int albumCount;
    private ZonedDateTime establishmentDate;
    private StudioCreateDto studio;
}
