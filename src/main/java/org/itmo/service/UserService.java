package org.itmo.service;

import org.itmo.model.User;
import org.itmo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- НОВЫЙ ИМПОРТ
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // <-- ОБЯЗАТЕЛЬНОЕ ВНЕДРЕНИЕ КОДИРОВЩИКА!

    /**
     * Загружает пользователя по имени.
     * ЭТОТ МЕТОД ВЫЗЫВАЕТСЯ ПРИ ЗАПРОСЕ С КЛИЕНТА, ЕСЛИ BASIC AUTH СРАБАТЫВАЕТ.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Лог 1: Должен появиться при запросе с клиента
        log.info("Attempting to load user by username: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: '{}'", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        // --- ВРЕМЕННАЯ ДИАГНОСТИКА: ПРОВЕРКА ЦЕЛОСТНОСТИ ХЭША (ЭТО ГЛАВНОЕ) ---
        // Пароль, который мы ожидаем увидеть
        String expectedPassword = username.equals("admin") ? "adminpass" : "userpass";
        String storedHash = user.getPassword();

        // Проверяем, что хэш, хранящийся в БД, соответствует ожидаемому паролю
        boolean hashCheck = passwordEncoder.matches(expectedPassword, storedHash);

        log.warn("DIAGNOSTIC HASH CHECK for '{}'. Expected Pass: '{}'. Stored Hash: '{}'. Result: {}",
                username, expectedPassword, storedHash, hashCheck);

        if (!hashCheck) {
            log.error("CRITICAL ERROR: Stored hash for user '{}' DOES NOT MATCH expected password '{}'. This will result in 401.",
                    username, expectedPassword);
        }
        // --- КОНЕЦ ДИАГНОСТИКИ ---

        return user;
    }

    /**
     * Метод для тестового создания пользователей при старте приложения.
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}