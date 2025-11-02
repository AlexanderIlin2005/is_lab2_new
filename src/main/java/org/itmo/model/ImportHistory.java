package org.itmo.model;

import org.itmo.model.enums.ImportStatus;
// ДОБАВИТЬ ЭТОТ ИМПОРТ

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "import_history")
@Getter
@Setter
@NoArgsConstructor
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "launched_by_id", nullable = false)
    private User launchedBy;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime = ZonedDateTime.now();

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    // 💡 ИСПРАВЛЕНИЕ: Переход на хранение в виде стандартной строки
    // 1. УДАЛЕНО: @Convert(converter = ImportStatusConverter.class)
    // 2. ВОЗВРАЩЕНО: @Enumerated(EnumType.STRING)
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            // Теперь это обычная строка (VARCHAR)
            columnDefinition = "VARCHAR(50)"
    )
    private ImportStatus status = ImportStatus.PENDING;

    @Column(name = "added_count")
    private Integer addedCount;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    public ImportHistory(User launchedBy) {
        this.launchedBy = launchedBy;
    }
}