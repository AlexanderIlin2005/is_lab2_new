package org.itmo.controller;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.model.User;
import org.itmo.service.ImportHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/import-history")
public class ImportHistoryController {

    private final ImportHistoryService historyService;

    public ImportHistoryController(ImportHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<List<ImportHistoryResponseDto>> getHistory(@AuthenticationPrincipal User currentUser) {

        // Роль ADMIN определяется по имени роли
        boolean isAdmin = currentUser != null && currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        List<ImportHistoryResponseDto> history = historyService.getImportHistory(currentUser, isAdmin);

        return ResponseEntity.ok(history);
    }
}