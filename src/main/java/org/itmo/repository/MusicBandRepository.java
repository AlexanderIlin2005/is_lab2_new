package org.itmo.repository;

import org.itmo.model.MusicBand;
import org.itmo.model.MusicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MusicBandRepository extends JpaRepository<MusicBand, Long> {
    Page<MusicBand> findByName(String name, Pageable pageable);


    Page<MusicBand> findByGenre(MusicGenre genre, Pageable pageable);

    long deleteByStudioName(String studioName);

    Optional<MusicBand> findFirstByStudioName(String studioName);

    Optional<MusicBand> findByNameAndGenre(String name, MusicGenre genre);

    @Query("SELECT AVG(m.albumCount) FROM MusicBand m")
    Double findAverageAlbumCount();

    @Query("SELECT COUNT(m) FROM MusicBand m WHERE m.studio.name > :studioName")
    long countByStudioNameGreaterThan(@Param("studioName") String studioName);


    @Query("SELECT m FROM MusicBand m WHERE m.genre = :genre")
    List<MusicBand> findByGenre(@Param("genre") MusicGenre genre);


}
