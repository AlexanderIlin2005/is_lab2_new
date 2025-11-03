package org.itmo.controller;

import jakarta.validation.Valid;
import org.itmo.dto.MusicBandCreateDto;
import org.itmo.dto.MusicBandResponseDto;
import org.itmo.dto.ImportResultDto;
import org.itmo.model.MusicGenre;
import org.itmo.service.MusicBandService;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.Map;

import java.util.List;
import java.util.Map;


import jakarta.validation.ValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI; 

@RestController
@RequestMapping("/api/music-bands")
public class MusicBandController {
    private final MusicBandService musicBandService;

    public MusicBandController(MusicBandService musicBandService) {
        this.musicBandService = musicBandService;
    }

    

    @GetMapping
    public Page<MusicBandResponseDto> list(Pageable pageable,
                                           @RequestParam(required = false) String nameEquals) {
        return musicBandService.list(nameEquals, pageable);
    }

    @GetMapping("/{id}")
    public MusicBandResponseDto get(@PathVariable Long id) {
        return musicBandService.get(id);
    }

    
    
    
    
    

    @PostMapping

    public ResponseEntity<MusicBandResponseDto> create(@Valid @RequestBody MusicBandCreateDto musicBand) {
        MusicBandResponseDto createdBand = musicBandService.create(musicBand);

        
        return ResponseEntity.created(
                URI.create("/api/music-bands/" + createdBand.getId())
        ).body(createdBand);
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

    @PostMapping("/import/xml")
    public ResponseEntity<Map<String, Object>> importXml(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("imported", 0, "message", "Файл пуст."));
        }

        try {
            
            ImportResultDto resultDto = musicBandService.importBandsFromXml(file.getInputStream());

            
            return ResponseEntity.ok(Map.of(
                    "imported", resultDto.getImported(),
                    "message", resultDto.getMessage(),
                    "success", resultDto.isSuccess()
            ));
        } catch (RuntimeException e) {
            
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("imported", 0, "error", e.getMessage()));
        } catch (IOException e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("imported", 0, "error", "Не удалось прочитать файл: " + e.getMessage()));
        }
    }

    
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleValidationException(ValidationException ex) {
        return Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage() 
        );
    }
}
