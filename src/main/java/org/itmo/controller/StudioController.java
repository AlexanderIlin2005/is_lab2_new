package org.itmo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.itmo.model.Studio;
import org.itmo.service.StudioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {
    private final StudioService studioService;

    @GetMapping
    public List<Studio> getAll() {
        return studioService.getAll();
    }

    @GetMapping("/{id}")
    public Studio getById(@PathVariable Long id) {
        return studioService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Studio create(@Valid @RequestBody Studio studio) {
        return studioService.create(studio);
    }

    @PatchMapping("/{id}")
    public Studio update(@PathVariable Long id, @Valid @RequestBody Studio studio) {
        return studioService.update(id, studio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        studioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
