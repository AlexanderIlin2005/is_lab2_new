package org.itmo.config;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import org.itmo.repository.UserRepository; // Импорт репозитория
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional; // Новый импорт для Optional

/**
 * Инициализатор данных: добавляет тестовых пользователей в БД при старте.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements InitializingBean {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * Вызывается после инициализации всех свойств бина.
     * Используется для инициализации данных.
     */
    @Override
    @Transactional
    public void afterPropertiesSet() throws Exception {

        // *** ВАЖНО: userRepository.deleteAll() УДАЛЕНО, чтобы не стирать существующие данные ***

        log.info("-> Starting data initialization...");

        // Создание или проверка существования пользователей для JMeter:

        // Администраторы (admin, adminpass и admin2, adminpass2)
        createUserIfNotExist("admin", "adminpass", UserRole.ADMIN);
        createUserIfNotExist("admin2", "adminpass2", UserRole.ADMIN);

        // Обычные пользователи (user, userpass и user2, userpass2)
        createUserIfNotExist("user", "userpass", UserRole.USER);
        createUserIfNotExist("user2", "userpass2", UserRole.USER);

        log.info("-> Data initialization complete. All required test users created/verified.");

        // *************************************************************
        // !!! ДИАГНОСТИКА: ПРОВЕРКА loadUserByUsername ПРИ СТАРТЕ !!!
        // *************************************************************
        /*
        try {
            log.info("--- START DIAGNOSTIC: Testing loadUserByUsername ---");
            UserDetails adminDetails = userService.loadUserByUsername("admin");

            if (adminDetails != null) {
                log.info("DIAGNOSTIC SUCCESS: User 'admin' loaded successfully. Authorities: {}", adminDetails.getAuthorities());
            } else {
                log.error("DIAGNOSTIC ERROR: loadUserByUsername returned null.");
            }
        } catch (UsernameNotFoundException e) {
            log.error("DIAGNOSTIC CRITICAL FAIL: User 'admin' was not found immediately after creation!", e);
        }
        log.info("--- END DIAGNOSTIC ---");

         */

    }

    /**
     * Создает пользователя или ОБНОВЛЯЕТ его пароль, если он уже существует.
     */
    private void createUserIfNotExist(String username, String rawPassword, UserRole role) {
        Optional<User> existingUserOpt = userRepository.findByUsername(username);
        User user;
        String action;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            action = "Updated password for";
        } else {
            user = new User();
            user.setUsername(username);
            user.setRole(role);
            action = "Created new user";
        }

        // *** КРИТИЧЕСКИЙ ШАГ: ВСЕГДА УСТАНАВЛИВАЕМ НОВЫЙ ХЭШ ПАРОЛЯ ***
        // (Используйте ваш реальный метод установки хеша: setPasswordHash или setPassword)
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        // **************************************************************

        userService.save(user);
        log.info("{} user: {} with role {}", action, username, role.name());
    }
}