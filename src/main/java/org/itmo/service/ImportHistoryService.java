package org.itmo.service;

import org.itmo.dto.ImportHistoryResponseDto;
import org.itmo.mapper.ImportHistoryMapper;
import org.itmo.model.ImportHistory;
import org.itmo.model.User;
import org.itmo.repository.ImportHistoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.PlatformTransactionManager; // New Import
import org.springframework.transaction.support.TransactionTemplate; // New Import

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ImportHistoryService {

    private final ImportHistoryRepository historyRepository;
    private final ImportHistoryMapper historyMapper;

    private final TransactionTemplate transactionTemplate; // ✅ Добавлено: Управление транзакцией

    public ImportHistoryService(ImportHistoryRepository historyRepository, ImportHistoryMapper historyMapper,
                                PlatformTransactionManager transactionManager) {
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;

        // ✅ Инициализация TransactionTemplate для выполнения в новой транзакции
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * ✅ Новый метод: Сохраняет историю в новой, независимой транзакции.
     * Это гарантирует, что запись будет закоммичена, даже если основной импорт откатится.
     */
    public ImportHistory saveHistoryInNewTransaction(ImportHistory history) {
        return transactionTemplate.execute(status -> {
            return historyRepository.save(history);
        });
    }

    
    public List<ImportHistoryResponseDto> getImportHistory(User currentUser, boolean isAdmin) {
        Sort sort = Sort.by(Sort.Direction.DESC, "startTime");

        if (isAdmin) {
            
            return historyRepository.findAll(sort).stream()
                    .map(historyMapper::toResponseDto) 
                    .collect(Collectors.toList());
        } else {
            
            if (currentUser == null) {
                return List.of();
            }
            return historyRepository.findByLaunchedBy(currentUser, sort).stream()
                    .map(historyMapper::toResponseDto) 
                    .collect(Collectors.toList());
        }
    }
}