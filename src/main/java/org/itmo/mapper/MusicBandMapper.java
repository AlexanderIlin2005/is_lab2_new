package org.itmo.mapper;

import org.itmo.dto.*;
import org.itmo.model.*;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MusicBandMapper {

    
    @Mapping(source = "id", target = "id")
    MusicBandResponseDto toResponseDto(MusicBand musicBand);

    
    @Mapping(source = "id", target = "id")
    CoordinatesResponseDto toResponseDto(Coordinates coordinates);

    AlbumResponseDto toResponseDto(Album album);
    StudioResponseDto toResponseDto(Studio studio);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coordinates", ignore = true)
    @Mapping(target = "bestAlbum", ignore = true)
    @Mapping(target = "studio", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    MusicBand toEntity(MusicBandCreateDto dto);

    Coordinates toEntity(CoordinatesCreateDto dto);
    Album toEntity(AlbumCreateDto dto);
    Studio toEntity(StudioCreateDto dto);
}
