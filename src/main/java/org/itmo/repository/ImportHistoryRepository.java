package org.itmo.repository;

import org.itmo.model.ImportHistory;
import org.itmo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    
    List<ImportHistory> findAllByLaunchedByOrderByIdDesc(User user);

    
    List<ImportHistory> findAllByOrderByIdDesc();

    
    List<ImportHistory> findByLaunchedBy(User launchedBy, Sort sort);
}