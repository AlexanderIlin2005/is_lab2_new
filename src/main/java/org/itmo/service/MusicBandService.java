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

import org.itmo.dto.MusicBandCreateDto;
import java.io.InputStream;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.JAXBException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.ValidationException;

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

    public MusicBandService(MusicBandRepository musicBandRepository,
                            AlbumRepository albumRepository,
                            CoordinatesRepository coordinatesRepository,
                            StudioRepository studioRepository,
                            MusicBandMapper musicBandMapper,
                            SimpMessagingTemplate messagingTemplate) {
        this.musicBandRepository = musicBandRepository;
        this.albumRepository = albumRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.studioRepository = studioRepository;
        //this.eventsPublisher = eventsPublisher;
        this.musicBandMapper = musicBandMapper;
        this.messagingTemplate = messagingTemplate;
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


        musicBand = musicBandRepository.save(musicBand);
        notifyClients("BAND_UPDATED"); // <-- Добавить вызов
        return musicBandMapper.toResponseDto(musicBand);
    }

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

    /**
     * НОВЫЙ МЕТОД:
     * Ручная валидация DTO из XML на соответствие новым бизнес-правилам.
     */
    private void validateDtoForImport(MusicBandCreateDto dto, int index) {
        String prefix = "Группа #" + (index + 1) + " (" + (dto.getName() != null ? dto.getName() : "N/A") + "): ";

        // name: Поле не может быть null, Строка не может быть пустой
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException(prefix + "Поле 'name' не может быть null или пустым.");
        }

        // coordinates: Поле не может быть null
        if (dto.getCoordinates() == null) {
            throw new ValidationException(prefix + "Поле 'coordinates' не может быть null.");
        }

        // genre: Поле не может быть null
        if (dto.getGenre() == null) {
            throw new ValidationException(prefix + "Поле 'genre' не может быть null.");
        }

        // numberOfParticipants: Значение поля должно быть больше 0
        if (dto.getNumberOfParticipants() <= 0) {
            throw new ValidationException(prefix + "Поле 'numberOfParticipants' должно быть > 0.");
        }

        // singlesCount: Значение поля должно быть больше 0 (в DTO это Long)
        if (dto.getSingleCount() == null || dto.getSingleCount() <= 0) {
            throw new ValidationException(prefix + "Поле 'singlesCount' не может быть null и должно быть > 0.");
        }

        // description: Поле не может быть null <-- ИЗМЕНЕНО: убрали пояснение в скобках
        if (dto.getDescription() == null) {
            throw new ValidationException(prefix + "Поле 'description' не может быть null.");
        }

        // albumsCount: Поле не может быть null, Значение поля должно быть больше 0 (в DTO это int)
        if (dto.getAlbumCount() <= 0) {
            throw new ValidationException(prefix + "Поле 'albumCount' должно быть > 0.");
        }

        // establishmentDate: Поле не может быть null
        if (dto.getEstablishmentDate() == null) {
            throw new ValidationException(prefix + "Поле 'establishmentDate' не может быть null.");
        }
    }


    /**
     * ИЗМЕНЕННЫЙ МЕТОД ИМПОРТА
     */
    @Transactional
    public int importBandsFromXml(InputStream xmlData) {
        try {
            // 1. Парсинг XML
            JAXBContext jaxbContext = JAXBContext.newInstance(MusicBandListWrapper.class, MusicBandCreateDto.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            MusicBandListWrapper wrapper = (MusicBandListWrapper) unmarshaller.unmarshal(xmlData);
            List<MusicBandCreateDto> dtos = wrapper.getMusicBands();

            if (dtos == null || dtos.isEmpty()) {
                return 0;
            }

            // 2. ВАЛИДАЦИЯ (Requirement 1)
            // Сначала проверяем *все* DTO на соответствие бизнес-правилам.
            // Если хотя бы один не пройдет, транзакция прервется до вставки.
            for (int i = 0; i < dtos.size(); i++) {
                MusicBandCreateDto dto = dtos.get(i);

                // Проверка DTO на NOT NULL и > 0
                validateDtoForImport(dto, i);

                // НОВАЯ ПРОВЕРКА УНИКАЛЬНОСТИ
                checkUniqueness(dto, null); // Импорт: ID для исключения = null
            }

            // 3. СОЗДАНИЕ (Requirement 2)
            // Все DTO валидны, теперь создаем их.
            // Метод create() выполнит проверки (например, "Studio not found")
            // Если он упадет, @Transactional откатит все предыдущие.
            int count = 0;
            for (MusicBandCreateDto dto : dtos) {
                this.create(dto); // Используем существующий метод create
                count++;
            }

            notifyClients("BAND_BULK_IMPORTED");
            return count;

        } catch (JAXBException e) {
            throw new RuntimeException("Ошибка парсинга XML: " + e.getMessage(), e);
        } catch (ValidationException | EntityNotFoundException e) { // Ловим ошибки валидации или create()
            throw new RuntimeException("Ошибка импорта (транзакция будет отменена): " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Неизвестная ошибка (транзакция будет отменена): " + e.getMessage(), e);
        }
    }

    /**
     * НОВЫЙ МЕТОД:
     * Проверяет, что не существует другой группы с таким же именем И жанром,
     * исключая группу с bandIdToExclude.
     *
     * ВНИМАНИЕ: Реализация является НЕЭФФЕКТИВНОЙ, так как использует существующий
     * метод findByGenre() и выполняет фильтрацию в памяти (чтобы не менять интерфейс репозитория).
     * @param dto DTO создаваемой/обновляемой группы.
     * @param bandIdToExclude ID группы, которую нужно исключить из проверки (null при создании).
     */
    private void checkUniqueness(MusicBandCreateDto dto, Long bandIdToExclude) {
        // Эти проверки должны пройти в validateDtoForImport, но проверяем на всякий случай
        if (dto.getName() == null || dto.getGenre() == null) {
            return;
        }

        // 1. Получаем все группы с таким же жанром
        List<MusicBand> bandsOfSameGenre = musicBandRepository.findByGenre(dto.getGenre());

        // 2. Проверяем, есть ли среди них группа с точно таким же именем,
        //    исключая группу с ID, которую мы обновляем.
        boolean exists = bandsOfSameGenre.stream()
                // Исключаем текущую группу при обновлении:
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