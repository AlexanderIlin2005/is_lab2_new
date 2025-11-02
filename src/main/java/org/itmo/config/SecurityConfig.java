package org.itmo.config;

import org.itmo.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- НОВЫЙ ИМПОРТ
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// !!! НОВЫЕ ИМПОРТЫ ДЛЯ ЯВНОЙ BASIC АУТЕНТИФИКАЦИИ
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
// !!!

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement(proxyTargetClass = true)
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Внедряем PasswordEncoder

    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("Route Manager API Realm");
        return entryPoint;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 1. ЯВНЫЙ AuthenticationManager (КРИТИЧЕСКИЙ ШАГ)
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Привязываем наш UserDetailsService
        authProvider.setUserDetailsService(userService);
        // Привязываем наш PasswordEncoder
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    // 2. ЯВНЫЙ BasicAuthenticationFilter
    @Bean
    public BasicAuthenticationFilter basicAuthenticationFilter(HttpSecurity http) throws Exception {
        // Используем наш явный AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManager();
        return new BasicAuthenticationFilter(authenticationManager, authenticationEntryPoint());
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // !!! 3. ОТКЛЮЧАЕМ АВТОМАТИЧЕСКИЙ BASIC И ВНЕДРЯЕМ НАШ ФИЛЬТР
                .httpBasic(AbstractHttpConfigurer::disable)
                // !!! ИСПРАВЛЕНИЕ: Используем addFilterAt для принудительного размещения !!!
                .addFilterAt(basicAuthenticationFilter(http), BasicAuthenticationFilter.class)

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Настройки авторизации (остаются без изменений)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html", "/ws/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/music-bands/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/music-bands").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/music-bands/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/music-bands/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/music-bands/import/xml").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/import-history").authenticated()
                        .requestMatchers("/api/studios/**", "/api/albums/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}