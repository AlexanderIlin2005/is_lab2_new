package org.itmo.service;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.mapper.ImportHistoryMapper;
import org.itmo.model.ImportHistory;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.ImportHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportHistoryService {

    private final ImportHistoryRepository historyRepository;
    private final ImportHistoryMapper historyMapper;

    /**
     * Получает историю импорта с учетом роли пользователя.
     * @param currentUser Пользователь, выполняющий запрос (полученный из Spring Security).
     * @return Список DTO истории импорта.
     */
    public List<ImportHistoryResponseDto> getImportHistory(User currentUser) {
        List<ImportHistory> history;

        // Администратор видит всю историю
        if (currentUser.getRole() == UserRole.ADMIN) {
            history = historyRepository.findAllByOrderByIdDesc();
        }
        // Обычный пользователь видит только свои операции
        else {
            history = historyRepository.findAllByLaunchedByOrderByIdDesc(currentUser);
        }

        return historyMapper.toResponseDto(history);
    }
}