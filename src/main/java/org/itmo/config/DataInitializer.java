package org.itmo.config;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean; // НОВЫЙ ИМПОРТ
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Важно для создания пользователей

/**
 * Инициализатор данных: добавляет тестовых пользователей в БД при старте,
 * используя стандартный интерфейс Spring'а InitializingBean.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements InitializingBean { // ИМПЛЕМЕНТИРУЕМ InitializingBean

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Этот метод будет выполнен Spring-контейнером после инициализации всех свойств бина.
     */
    @Override
    @Transactional
    public void afterPropertiesSet() throws Exception {

        // ВАЖНО: loadUserByUsername выбросит исключение, если пользователь не найден.
        // Чтобы избежать этого, используем try-catch.

        // 1. Создание тестового пользователя ADMIN (admin/adminpass)
        try {
            userService.loadUserByUsername("admin");
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            User admin = new User();
            admin.setUsername("admin");
            // Пароль: 'adminpass'
            admin.setPasswordHash(passwordEncoder.encode("adminpass"));
            admin.setRole(UserRole.ADMIN);
            userService.save(admin);
            System.out.println("-> Created initial ADMIN user: admin/adminpass");
        }

        // 2. Создание тестового пользователя USER (user/userpass)
        try {
            userService.loadUserByUsername("user");
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            User user = new User();
            user.setUsername("user");
            // Пароль: 'userpass'
            user.setPasswordHash(passwordEncoder.encode("userpass"));
            user.setRole(UserRole.USER);
            userService.save(user);
            System.out.println("-> Created initial USER: user/userpass");
        }
    }
}