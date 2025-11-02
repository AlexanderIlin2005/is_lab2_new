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
    }
}