package org.itmo.config;

import org.itmo.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
// Удален импорт AuthenticationManager, т.к. мы его больше не определяем
// Удален импорт AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint; // Оставляем
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.cors.CorsConfiguration; // <-- НОВЫЙ ИМПОРТ
import org.springframework.web.cors.CorsConfigurationSource; // <-- НОВЫЙ ИМПОРТ
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // <-- НОВЫЙ ИМПОРТ
import java.util.Arrays; // <-- НОВЫЙ ИМПОРТ
import java.util.List; // <-- НОВЫЙ ИМПОРТ

@Configuration
@EnableWebSecurity
@EnableTransactionManagement(proxyTargetClass = true)
public class SecurityConfig {

    @Autowired
    private UserService userService;

    // !!! AuthenticationManager удален, что правильно. !!!

    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("My Realm");
        return entryPoint;
    }

    // !!! ШАГ 1: НОВЫЙ БИН ДЛЯ CORS (ОБЯЗАТЕЛЬНО ДЛЯ REST API) !!!
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Разрешаем все методы, заголовки и источники (для простоты)
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Важно для Basic Auth

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // !!! ШАГ 2: ПРИМЕНЯЕМ CORS ВНУТРИ SECURITY !!!
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Отключаем CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // !!! ИЗМЕНЕНИЕ: Включаем Basic Auth Filter, но без настройки EntryPoint здесь !!!
                .httpBasic(httpBasic -> {})

                // !!! КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: ЯВНО НАЗНАЧАЕМ ENTRYPOINT
                // ДЛЯ НЕАУТЕНТИФИЦИРОВАННЫХ ЗАПРОСОВ В БЛОКЕ EXCEPTION HANDLING
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                )

                // Сессия Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Настройка правил авторизации
                .authorizeHttpRequests(auth -> auth
                        // 1. РАЗРЕШАЕМ ВСЕ OPTIONS (Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Разрешаем доступ к статическим ресурсам и WebSocket
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