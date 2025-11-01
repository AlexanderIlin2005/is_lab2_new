package org.itmo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Настройка SecurityFilterChain (правила доступа)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF для простоты (для API это часто делают)
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем доступ к статическим ресурсам и WebSocket без аутентификации
                        .requestMatchers("/", "/index.html", "/topic/**", "/api/bands/import-xml").permitAll()

                        // Эндпоинт истории импорта требует аутентификации
                        .requestMatchers("/api/import-history").authenticated()

                        // Настройка доступа к остальным API (пример)
                        .requestMatchers("/api/bands/**").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                // Включаем Basic Auth или Form Login
                .httpBasic(withDefaults())
        // или .formLogin(withDefaults())
        ;

        return http.build();
    }

    // 2. Бин для шифрования паролей (обязательно для Spring Security)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}