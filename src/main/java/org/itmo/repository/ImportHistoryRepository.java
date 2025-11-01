package org.itmo.repository;

import org.itmo.model.ImportHistory;
import org.itmo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    // Получение истории по пользователю, сортировка по убыванию ID (новее сверху)
    List<ImportHistory> findAllByLaunchedByOrderByIdDesc(User user);

    // Получение всей истории (для ADMIN)
    List<ImportHistory> findAllByOrderByIdDesc();

    // НОВЫЙ МЕТОД: Поиск по пользователю, который запустил импорт
    List<ImportHistory> findByLaunchedBy(User launchedBy, Sort sort);
}