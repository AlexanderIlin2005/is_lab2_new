package org.itmo.config;

import org.itmo.service.UserService; // ОК, теперь UserService существует
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.transaction.annotation.EnableTransactionManagement; // <--- НОВЫЙ ИМПОРТ

@Configuration
@EnableWebSecurity
@EnableTransactionManagement(proxyTargetClass = true) // <--- ИСПРАВЛЕНИЕ: Включаем CGLIB проксирование
public class SecurityConfig {

    private final UserService userService; // ОК, теперь UserService существует

    public SecurityConfig(UserService userService) { // ОК, теперь UserService существует
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userService) // Используем ваш UserService для загрузки данных пользователя
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF, так как это REST API
                .csrf(AbstractHttpConfigurer::disable)
                // Используем Basic Auth (логин/пароль)
                .httpBasic(httpBasic -> {})
                // Сессия Stateless (без сохранения состояния на сервере)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Настройка правил авторизации (с использованием .hasAuthority, т.к. UserDetails возвращает роли как GrantedAuthority)
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем доступ к статическим ресурсам и WebSocket
                        .requestMatchers("/", "/index.html", "/ws/**").permitAll()

                        // Music Bands: чтение всем, остальные операции - аутентифицированным
                        .requestMatchers(HttpMethod.GET, "/api/music-bands/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/music-bands").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/music-bands/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/music-bands/**").hasAuthority("ROLE_ADMIN")

                        // Импорт XML - только ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/music-bands/import/xml").hasAuthority("ROLE_ADMIN")

                        // История импорта - только аутентифицированным
                        .requestMatchers(HttpMethod.GET, "/api/import-history").authenticated()

                        // Studios & Albums - все операции только для аутентифицированных
                        .requestMatchers("/api/studios/**", "/api/albums/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        // Все остальные запросы должны быть аутентифицированы
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}