package org.itmo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "music_band")
public class MusicBand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(1)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinates_id", nullable = false)
    @NotNull
    private Coordinates coordinates;

    @Column(name = "creation_date", updatable = false, nullable = false)
    private ZonedDateTime creationDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private MusicGenre genre;

    @Min(1)
    @Column(name = "number_of_participants")
    private long numberOfParticipants;

    @Min(1)
    @Column(name = "single_count")
    private Long singleCount;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "best_album_id")
    private Album bestAlbum;

    @Min(1)
    @Column(name = "album_count")
    private int albumCount;

    @NotNull
    @Column(name = "establishment_date")
    private ZonedDateTime establishmentDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studio_id")
    private Studio studio;

    @Version // <--- КРИТИЧЕСКАЯ АННОТАЦИЯ
    private Long version; // <--- Поле для хранения номера версии

    @PrePersist
    protected void onCreate() {
        this.creationDate = ZonedDateTime.now();
    }
}

