package org.itmo.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name = "musicBands")
public class MusicBandListWrapper {

    
    private List<MusicBandCreateDto> musicBands;

    @XmlElement(name = "musicBand")
    public List<MusicBandCreateDto> getMusicBands() {
        return musicBands;
    }

    public void setMusicBands(List<MusicBandCreateDto> musicBands) {
        this.musicBands = musicBands;
    }

    public MusicBandListWrapper() {}
}