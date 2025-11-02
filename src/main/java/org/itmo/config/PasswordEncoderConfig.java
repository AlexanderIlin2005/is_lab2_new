package org.itmo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // <-- Вернуть BCrypt
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // !!! ВОЗВРАЩАЕМ БЕЗОПАСНОЕ ХЕШИРОВАНИЕ !!!
        return new BCryptPasswordEncoder();
    }
}