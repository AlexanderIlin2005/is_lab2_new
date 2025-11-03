package org.itmo.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.itmo.dto.*;
import org.itmo.mapper.MusicBandMapper;
import org.itmo.model.*;
import org.itmo.repository.MusicBandRepository;
import org.itmo.repository.AlbumRepository;
import org.itmo.repository.CoordinatesRepository;
import org.itmo.repository.StudioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.security.core.context.SecurityContextHolder; // НОВЫЙ ИМПОРТ

import org.itmo.dto.MusicBandCreateDto;
import java.io.InputStream;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.JAXBException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.ZonedDateTime; // НОВЫЙ ИМПОРТ

import jakarta.validation.ValidationException;

// НОВЫЕ ИМПОРТЫ ДЛЯ ЛОГИРОВАНИЯ ИСТОРИИ
import org.itmo.model.enums.ImportStatus;
import org.itmo.repository.ImportHistoryRepository;
import org.itmo.repository.UserRepository;



@Service
@Transactional
public class MusicBandService {
    private final MusicBandRepository musicBandRepository;
    private final AlbumRepository albumRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final StudioRepository studioRepository;
    //private final MusicBandEventsPublisher eventsPublisher;
    private final MusicBandMapper musicBandMapper;
    private final SimpMessagingTemplate messagingTemplate;
    // НОВЫЕ ПОЛЯ
    private final ImportHistoryRepository historyRepository;
    private final UserRepository userRepository; // Нужно для загрузки пользователя в случае отсутствия Security

    public MusicBandService(MusicBandRepository musicBandRepository,
                            AlbumRepository albumRepository,
                            CoordinatesRepository coordinatesRepository,
                            StudioRepository studioRepository,
                            MusicBandMapper musicBandMapper,
                            SimpMessagingTemplate messagingTemplate,
                            // НОВЫЕ АРГУМЕНТЫ КОНСТРУКТОРА
                            ImportHistoryRepository historyRepository,
                            UserRepository userRepository) {
        this.musicBandRepository = musicBandRepository;
        this.albumRepository = albumRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.studioRepository = studioRepository;
        //this.eventsPublisher = eventsPublisher;
        this.musicBandMapper = musicBandMapper;
        this.messagingTemplate = messagingTemplate;

        // ИНИЦИАЛИЗАЦИЯ НОВЫХ ПОЛЕЙ
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }


    private void notifyClients(String type) {

        messagingTemplate.convertAndSend("/topic/bands/updates", type);
    }

    public Page<MusicBandResponseDto> list(String nameEquals, Pageable pageable) {
        if (nameEquals != null && !nameEquals.isEmpty()) {
            return musicBandRepository.findByName(nameEquals, pageable)
                    .map(musicBandMapper::toResponseDto);
        }
        return musicBandRepository.findAll(pageable).map(musicBandMapper::toResponseDto);
    }

    public MusicBandResponseDto get(@NotNull Long id) {
        MusicBand musicBand = musicBandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MusicBand not found: " + id));
        return musicBandMapper.toResponseDto(musicBand);
    }

    public MusicBandResponseDto create(@Valid MusicBandCreateDto dto) {
        // --- ИЗМЕНЕНИЕ ---
        checkUniqueness(dto, null); // Создание: ID для исключения = null
        // -----------------
        MusicBand musicBand = musicBandMapper.toEntity(dto);


        Long coordsId = dto.getCoordinates() != null ? dto.getCoordinates().getId() : null;
        if (coordsId != null) {
            Coordinates persistentCoords = coordinatesRepository.findById(coordsId)
                    .orElseThrow(() -> new EntityNotFoundException("Coordinates not found: " + coordsId));
            musicBand.setCoordinates(persistentCoords);
        } else if (dto.getCoordinates() != null) {
            Coordinates newCoords = musicBandMapper.toEntity(dto.getCoordinates());
            newCoords = coordinatesRepository.save(newCoords);
            musicBand.setCoordinates(newCoords);
        } else {
            throw new IllegalArgumentException("coordinates are required");
        }


        Long albumId = dto.getBestAlbum() != null ? dto.getBestAlbum().getId() : null;
        if (albumId != null) {
            Album persistentAlbum = albumRepository.findById(albumId)
                    .orElseThrow(() -> new EntityNotFoundException("Album not found: " + albumId));
            musicBand.setBestAlbum(persistentAlbum);
        } else if (dto.getBestAlbum() != null) {
            Album newAlbum = musicBandMapper.toEntity(dto.getBestAlbum());
            newAlbum = albumRepository.save(newAlbum);
            musicBand.setBestAlbum(newAlbum);
        }


        Long studioId = dto.getStudio() != null ? dto.getStudio().getId() : null;
        if (studioId != null) {
            Studio persistentStudio = studioRepository.findById(studioId)
                    .orElseThrow(() -> new EntityNotFoundException("Studio not found: " + studioId));
            musicBand.setStudio(persistentStudio);
        }


        // 1. Сохраняем сущность (она все еще, вероятно, имеет id=null)
        musicBand = musicBandRepository.save(musicBand);

        // 2. АГРЕССИВНАЯ ПРОВЕРКА / ПОЛУЧЕНИЕ ID ИЗ БД
        Long realBandId = musicBand.getId();

        // Если ID по-прежнему NULL (что является багом, который мы обходим)
        if (realBandId == null) {
            // Выполняем SQL-запрос через репозиторий, чтобы получить ID
            MusicBand persistedBand = musicBandRepository
                    .findByNameAndGenre(musicBand.getName(), musicBand.getGenre())
                    .orElseThrow(() -> new IllegalStateException("Группа была сохранена, но не найдена по Name/Genre. Критическая ошибка транзакции."));

            // Получаем ID из БД
            realBandId = persistedBand.getId();

            // 3. Обновляем объект в памяти, чтобы он соответствовал БД
            musicBand = persistedBand;
        }
        // К этому моменту realBandId ГАРАНТИРОВАННО содержит числовой ID.

        // 4. КОНСТРУИРОВАНИЕ DTO С ГАРАНТИРОВАННЫМ ID
        MusicBandResponseDto responseDto = musicBandMapper.toResponseDto(musicBand);

        // Убедимся, что ID скопирован, используя гарантированный realBandId
        responseDto.setId(realBandId);

        // Вложенные координаты, если их ID тоже был потерян
        if (musicBand.getCoordinates() != null && responseDto.getCoordinates() != null) {
            responseDto.getCoordinates().setId(musicBand.getCoordinates().getId());
        }

        notifyClients("BAND_UPDATED");
        return responseDto;
    }

    @Transactional
    public MusicBandResponseDto update(@NotNull Long id, @Valid MusicBandCreateDto patch) {
        MusicBand existing = musicBandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MusicBand not found: " + id));


        // --- ИЗМЕНЕНИЕ: РУЧНОЕ СОЗДАНИЕ DTO ДЛЯ ПРОВЕРКИ УНИКАЛЬНОСТИ ---
        MusicBandCreateDto futureDto = new MusicBandCreateDto();

        // Копируем текущее состояние (которое может быть null, если не задано)
        futureDto.setName(existing.getName());
        futureDto.setGenre(existing.getGenre());

        // Применяем изменения из patch для проверки
        if (patch.getName() != null) futureDto.setName(patch.getName());
        if (patch.getGenre() != null) futureDto.setGenre(patch.getGenre());

        // ПРОВЕРКА УНИКАЛЬНОСТИ (с исключением текущего ID)
        checkUniqueness(futureDto, id);
        // -----------------------------------------------------------------


        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getGenre() != null) existing.setGenre(patch.getGenre());
        if (patch.getNumberOfParticipants() > 0) existing.setNumberOfParticipants(patch.getNumberOfParticipants());
        if (patch.getSingleCount() != null) existing.setSingleCount(patch.getSingleCount());
        if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
        if (patch.getAlbumCount() > 0) existing.setAlbumCount(patch.getAlbumCount());
        if (patch.getEstablishmentDate() != null) existing.setEstablishmentDate(patch.getEstablishmentDate());


        if (patch.getCoordinates() != null) {
            Long coordsId = patch.getCoordinates().getId();
            if (coordsId != null) {
                Coordinates persistentCoords = coordinatesRepository.findById(coordsId)
                        .orElseThrow(() -> new EntityNotFoundException("Coordinates not found: " + coordsId));
                existing.setCoordinates(persistentCoords);
            } else {
                Coordinates newCoords = musicBandMapper.toEntity(patch.getCoordinates());
                newCoords = coordinatesRepository.save(newCoords);
                existing.setCoordinates(newCoords);
            }
        }


        if (patch.getBestAlbum() != null) {
            Long albumId = patch.getBestAlbum().getId();
            if (albumId != null) {
                Album persistentAlbum = albumRepository.findById(albumId)
                        .orElseThrow(() -> new EntityNotFoundException("Album not found: " + albumId));
                existing.setBestAlbum(persistentAlbum);
            } else {
                Album newAlbum = musicBandMapper.toEntity(patch.getBestAlbum());
                newAlbum = albumRepository.save(newAlbum);
                existing.setBestAlbum(newAlbum);
            }
        }


        if (patch.getStudio() != null) {
            Long studioId = patch.getStudio().getId();

            if (studioId != null) {

                Studio persistentStudio = studioRepository.findById(studioId)
                        .orElseThrow(() -> new EntityNotFoundException("Studio not found: " + studioId));
                existing.setStudio(persistentStudio);
            } else {

                existing.setStudio(null);
            }
        }


        existing = musicBandRepository.save(existing);
        notifyClients("BAND_UPDATED");
        return musicBandMapper.toResponseDto(existing);
    }

    @Transactional
    public void delete(@NotNull Long id) {
        MusicBand musicBand = musicBandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MusicBand not found: " + id));
        musicBandRepository.delete(musicBand);

        notifyClients("BAND_UPDATED");
    }

    // Специальные операции
    public boolean deleteOneByStudioName(String studioName) {
        Optional<MusicBand> musicBand = musicBandRepository.findFirstByStudioName(studioName);
        if (musicBand.isPresent()) {
            musicBandRepository.delete(musicBand.get());

            return true;
        }
        return false;
    }

    public Double getAverageAlbumCount() {
        return musicBandRepository.findAverageAlbumCount();
    }

    public long countByStudioNameGreaterThan(String studioName) {
        return musicBandRepository.countByStudioNameGreaterThan(studioName);
    }

    public List<MusicBandResponseDto> findByGenre(MusicGenre genre) {
        List<MusicBand> musicBands = musicBandRepository.findByGenre(genre);
        return musicBands.stream()
                .map(musicBandMapper::toResponseDto)
                .toList();
    }

    public long removeParticipant(Long id) {
        MusicBand musicBand = musicBandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MusicBand not found: " + id));

        if (musicBand.getNumberOfParticipants() > 1) {
            musicBand.setNumberOfParticipants(musicBand.getNumberOfParticipants() - 1);
            musicBandRepository.save(musicBand);

            notifyClients("BAND_UPDATED");
            return musicBand.getNumberOfParticipants();
        }
        return musicBand.getNumberOfParticipants();
    }

    // ВАШ МЕТОД validateDtoForImport (без изменений)
    private void validateDtoForImport(MusicBandCreateDto dto, int index) {
        String prefix = "Группа #" + (index + 1) + " (" + (dto.getName() != null ? dto.getName() : "N/A") + "): ";

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException(prefix + "Поле 'name' не может быть null или пустым.");
        }
        if (dto.getCoordinates() == null) {
            throw new ValidationException(prefix + "Поле 'coordinates' не может быть null.");
        }
        if (dto.getGenre() == null) {
            throw new ValidationException(prefix + "Поле 'genre' не может быть null.");
        }
        if (dto.getNumberOfParticipants() <= 0) {
            throw new ValidationException(prefix + "Поле 'numberOfParticipants' должно быть > 0.");
        }
        if (dto.getSingleCount() == null || dto.getSingleCount() <= 0) {
            throw new ValidationException(prefix + "Поле 'singlesCount' не может быть null и должно быть > 0.");
        }
        if (dto.getDescription() == null) {
            throw new ValidationException(prefix + "Поле 'description' не может быть null.");
        }
        if (dto.getAlbumCount() <= 0) {
            throw new ValidationException(prefix + "Поле 'albumCount' должно быть > 0.");
        }
        if (dto.getEstablishmentDate() == null) {
            throw new ValidationException(prefix + "Поле 'establishmentDate' не может быть null.");
        }
    }


    /**
     * ИЗМЕНЕННЫЙ МЕТОД ИМПОРТА С ЛОГИРОВАНИЕМ ИСТОРИИ
     */
    @Transactional
    public ImportResultDto importBandsFromXml(InputStream xmlData) {

        // --- 1. Определение текущего пользователя ---
        User currentUser = getCurrentAuthenticatedUser();
        ImportHistory history = null;

        if (currentUser != null) {
            history = new ImportHistory(currentUser);
            history = historyRepository.save(history); // Сохраняем PENDING
        }

        int importedCount = 0;

        try {
            // 2. Парсинг XML
            JAXBContext jaxbContext = JAXBContext.newInstance(MusicBandListWrapper.class, MusicBandCreateDto.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            MusicBandListWrapper wrapper = (MusicBandListWrapper) unmarshaller.unmarshal(xmlData);
            List<MusicBandCreateDto> dtos = wrapper.getMusicBands();

            if (dtos == null || dtos.isEmpty()) {
                if (history != null) {
                    history.setStatus(ImportStatus.SUCCESS);
                    history.setAddedCount(0);
                    history.setEndTime(ZonedDateTime.now());
                    historyRepository.save(history);
                }
                return new ImportResultDto(0, "XML-файл не содержит групп для импорта.", true);
            }

            // 3. ВАЛИДАЦИЯ и ПРОВЕРКА УНИКАЛЬНОСТИ
            for (int i = 0; i < dtos.size(); i++) {
                MusicBandCreateDto dto = dtos.get(i);
                validateDtoForImport(dto, i);
                checkUniqueness(dto, null);
            }

            // 4. СОЗДАНИЕ
            for (MusicBandCreateDto dto : dtos) {
                this.create(dto); // Используем существующий метод create
                importedCount++;
            }

            notifyClients("BAND_BULK_IMPORTED");

            // --- 5. ЛОГИРОВАНИЕ УСПЕХА (SUCCESS) ---
            if (history != null) {
                history.setStatus(ImportStatus.SUCCESS);
                history.setAddedCount(importedCount);
                history.setEndTime(ZonedDateTime.now());
                historyRepository.save(history);
            }

            return new ImportResultDto(importedCount, "Импорт завершен успешно.", true);

        } catch (JAXBException e) {
            handleImportError(history, "Ошибка парсинга XML: " + e.getMessage(), e);
            throw new RuntimeException("Ошибка парсинга XML: " + e.getMessage(), e);
        } catch (ValidationException | EntityNotFoundException e) {
            handleImportError(history, "Ошибка импорта (транзакция будет отменена): " + e.getMessage(), e);
            throw new RuntimeException("Ошибка импорта (транзакция будет отменена): " + e.getMessage(), e);
        } catch (Exception e) {
            handleImportError(history, "Неизвестная ошибка (транзакция будет отменена): " + e.getMessage(), e);
            throw new RuntimeException("Неизвестная ошибка (транзакция будет отменена): " + e.getMessage(), e);
        }
    }

    /**
     * Вспомогательный метод для получения текущего пользователя из контекста безопасности.
     */
    private User getCurrentAuthenticatedUser() {
        // Если Spring Security настроен и объект User имплементирует UserDetails
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }

        // ВАЖНО: Если аутентификация не настроена, возвращаем null.
        // В реальном приложении здесь должна быть ошибка аутентификации.
        return null;
    }

    /**
     * Вспомогательный метод для логирования ошибки.
     */
    private void handleImportError(ImportHistory history, String message, Exception e) {
        if (history != null) {
            history.setStatus(ImportStatus.FAILED);
            String errorMsg = e.getMessage() != null ? e.getMessage() : message;
            // Ограничиваем сообщение
            history.setErrorDetails(errorMsg.length() > 4096 ? errorMsg.substring(0, 4096) : errorMsg);
            history.setEndTime(ZonedDateTime.now());
            historyRepository.save(history);
        }
        // Логирование в консоль для дебага
        System.err.println(message + " Details: " + e.toString());
    }

    // ВАШ МЕТОД checkUniqueness (без изменений)
    private void checkUniqueness(MusicBandCreateDto dto, Long bandIdToExclude) {
        if (dto.getName() == null || dto.getGenre() == null) {
            return;
        }

        List<MusicBand> bandsOfSameGenre = musicBandRepository.findByGenre(dto.getGenre());

        boolean exists = bandsOfSameGenre.stream()
                .filter(band -> bandIdToExclude == null || !band.getId().equals(bandIdToExclude))
                .anyMatch(band -> dto.getName().equalsIgnoreCase(band.getName()));

        if (exists) {
            throw new ValidationException(
                    "Нарушение уникальности: Группа с названием '" + dto.getName() +
                            "' и жанром '" + dto.getGenre().name() + "' уже существует."
            );
        }
    }
}