package org.itmo.service;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.mapper.ImportHistoryMapper;
import org.itmo.model.ImportHistory;
import org.itmo.model.User;
import org.itmo.repository.ImportHistoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ImportHistoryService {

    private final ImportHistoryRepository historyRepository;
    private final ImportHistoryMapper historyMapper;

    public ImportHistoryService(ImportHistoryRepository historyRepository, ImportHistoryMapper historyMapper) {
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
    }

    /**
     * Возвращает историю импорта.
     * ADMIN видит всю историю, обычный USER - только свою.
     * @param currentUser Объект User, полученный из контекста безопасности.
     * @param isAdmin Флаг, указывающий, является ли пользователь ADMIN.
     * @return Список ImportHistoryResponseDto.
     */
    public List<ImportHistoryResponseDto> getImportHistory(User currentUser, boolean isAdmin) {
        Sort sort = Sort.by(Sort.Direction.DESC, "startTime");

        if (isAdmin) {
            // ADMIN видит всю историю
            return historyRepository.findAll(sort).stream()
                    .map(historyMapper::toResponseDto) // <-- ИСПРАВЛЕНО
                    .collect(Collectors.toList());
        } else {
            // Обычный пользователь видит только свою историю
            if (currentUser == null) {
                return List.of();
            }
            return historyRepository.findByLaunchedBy(currentUser, sort).stream()
                    .map(historyMapper::toResponseDto) // <-- ИСПРАВЛЕНО
                    .collect(Collectors.toList());
        }
    }
}