package org.itmo.config;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean; // НОВЫЙ ИМПОРТ
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Важно для создания пользователей
import org.itmo.repository.UserRepository; // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ ИЛИ @Autowired

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Инициализатор данных: добавляет тестовых пользователей в БД при старте,
 * используя стандартный интерфейс Spring'а InitializingBean.
 */
import org.itmo.repository.UserRepository; // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ ИЛИ @Autowired

@Component
@RequiredArgsConstructor
public class DataInitializer implements InitializingBean {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository; // <-- ДОБАВИТЬ ЭТО ПОЛЕ

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    @Transactional
    public void afterPropertiesSet() throws Exception {

        userRepository.deleteAll();

        // 2. Создание тестового пользователя ADMIN (admin/adminpass)
        User admin = new User();
        admin.setUsername("admin");
        // !!! ВОЗВРАЩАЕМ ХЕШИРОВАНИЕ !!!
        admin.setPasswordHash(passwordEncoder.encode("adminpass"));
        admin.setRole(UserRole.ADMIN);
        userService.save(admin);
        System.out.println("-> RESET: Created initial ADMIN user: admin/adminpass"); // Удалить (NoOp)

        // 3. Создание тестового пользователя USER (user/userpass)
        User user = new User();
        user.setUsername("user");
        // !!! ВОЗВРАЩАЕМ ХЕШИРОВАНИЕ !!!
        user.setPasswordHash(passwordEncoder.encode("userpass"));
        user.setRole(UserRole.USER);
        userService.save(user);
        System.out.println("-> RESET: Created initial USER: user/userpass"); // Удалить (NoOp)

        // *************************************************************
        // !!! ДИАГНОСТИКА: ПРОВЕРКА loadUserByUsername ПРИ СТАРТЕ !!!
        // *************************************************************
        try {
            log.info("--- START DIAGNOSTIC: Testing loadUserByUsername ---");
            UserDetails adminDetails = userService.loadUserByUsername("admin");

            // Мы не можем здесь проверить пароль, но можем проверить, что UserDetails не null
            if (adminDetails != null) {
                log.info("DIAGNOSTIC SUCCESS: User 'admin' loaded successfully. Authorities: {}", adminDetails.getAuthorities());
            } else {
                log.error("DIAGNOSTIC ERROR: loadUserByUsername returned null.");
            }
        } catch (UsernameNotFoundException e) {
            log.error("DIAGNOSTIC CRITICAL FAIL: User 'admin' was not found immediately after creation!");
        }
        log.info("--- END DIAGNOSTIC ---");
    }
}