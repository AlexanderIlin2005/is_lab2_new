package org.itmo.repository;

import org.itmo.model.ImportHistory;
import org.itmo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    // Получение истории по пользователю, сортировка по убыванию ID (новее сверху)
    List<ImportHistory> findAllByLaunchedByOrderByIdDesc(User user);

    // Получение всей истории (для ADMIN)
    List<ImportHistory> findAllByOrderByIdDesc();
}