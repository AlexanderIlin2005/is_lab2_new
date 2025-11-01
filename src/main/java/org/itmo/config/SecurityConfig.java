package org.itmo.config;

import org.itmo.service.UserService;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
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
                // Настройка правил авторизации
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем доступ к статическим ресурсам и WebSocket
                        .requestMatchers("/", "/index.html", "/ws/**").permitAll()

                        // Music Bands: чтение всем, остальные операции - аутентифицированным
                        .requestMatchers(HttpMethod.GET, "/api/music-bands/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/music-bands").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/music-bands/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/music-bands/**").hasAuthority("ADMIN")

                        // Импорт XML - только ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/music-bands/import/xml").hasAuthority("ADMIN")

                        // История импорта - только аутентифицированным
                        .requestMatchers(HttpMethod.GET, "/api/import-history").authenticated()

                        // Studios & Albums - все операции только для аутентифицированных
                        .requestMatchers("/api/studios/**", "/api/albums/**").hasAnyAuthority("USER", "ADMIN")

                        // Все остальные запросы должны быть аутентифицированы
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}