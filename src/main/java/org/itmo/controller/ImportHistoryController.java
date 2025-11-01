package org.itmo.controller;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.model.User;
import org.itmo.service.ImportHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/import-history")
@RequiredArgsConstructor
public class ImportHistoryController {

    private final ImportHistoryService historyService;

    // ПРИМЕЧАНИЕ: Для правильной работы @AuthenticationPrincipal(User currentUser)
    // требуется настроенный Spring Security и кастомный UserDetailsService,
    // возвращающий объект User. Без этого, currentUser будет null или
    // стандартный UserDetails.

    @GetMapping
    public ResponseEntity<List<ImportHistoryResponseDto>> getHistory(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            // Вернуть 401 Unauthorized, если пользователь не аутентифицирован
            return ResponseEntity.status(401).build();
        }

        List<ImportHistoryResponseDto> history = historyService.getImportHistory(currentUser);

        return ResponseEntity.ok(history);
    }
}