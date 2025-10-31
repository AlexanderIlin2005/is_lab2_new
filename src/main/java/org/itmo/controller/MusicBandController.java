package org.itmo.controller;

import jakarta.validation.Valid;
import org.itmo.dto.MusicBandCreateDto;
import org.itmo.dto.MusicBandResponseDto;
import org.itmo.model.MusicGenre;
import org.itmo.service.MusicBandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/music-bands")
public class MusicBandController {
    private final MusicBandService musicBandService;

    public MusicBandController(MusicBandService musicBandService) {
        this.musicBandService = musicBandService;
    }

    @GetMapping
    public Page<MusicBandResponseDto> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String sort,
                                           @RequestParam(required = false) String order,
                                           @RequestParam(required = false) String nameEquals) {
        Sort sortSpec = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            Sort.Direction dir = (order != null && order.equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortSpec = Sort.by(dir, sort);
        }
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        return musicBandService.list(nameEquals, pageable);
    }

    @GetMapping("/{id}")
    public MusicBandResponseDto get(@PathVariable Long id) {
        return musicBandService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MusicBandResponseDto create(@Valid @RequestBody MusicBandCreateDto musicBand) {
        return musicBandService.create(musicBand);
    }

    @PatchMapping("/{id}")
    public MusicBandResponseDto update(@PathVariable Long id,
                                       @Valid @RequestBody MusicBandCreateDto patch) {
        return musicBandService.update(id, patch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        musicBandService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/by-studio")
    public Map<String, Object> deleteOneByStudio(@RequestParam String studioName) {
        boolean deleted = musicBandService.deleteOneByStudioName(studioName);
        if (deleted) return Map.of("deleted", 1);
        return Map.of("deleted", 0);
    }

    @GetMapping("/average-album-count")
    public Map<String, Object> getAverageAlbumCount() {
        Double average = musicBandService.getAverageAlbumCount();
        return Map.of("average", average != null ? average : 0);
    }

    @GetMapping("/count-by-studio")
    public Map<String, Object> countByStudioNameGreaterThan(@RequestParam String studioName) {
        long count = musicBandService.countByStudioNameGreaterThan(studioName);
        return Map.of("count", count);
    }

    @GetMapping("/by-genre/{genre}")
    public List<MusicBandResponseDto> findByGenre(@PathVariable MusicGenre genre) {
        return musicBandService.findByGenre(genre);
    }

    @PatchMapping("/{id}/remove-participant")
    public Map<String, Object> removeParticipant(@PathVariable Long id) {
        long newCount = musicBandService.removeParticipant(id);
        return Map.of("numberOfParticipants", newCount);
    }
}
