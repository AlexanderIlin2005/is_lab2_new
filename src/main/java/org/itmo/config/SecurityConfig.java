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

// ... (другие импорты)
//import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
//import org.springframework.security.web.servlet.request.MvcRequestMatcher;
//import org.springframework.security.web.util.matcher.RequestMatcher;
// ...

import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // <-- НОВЫЙ ИМПОРТ

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


    // !!! ВАЖНО: УДАЛЕН АРГУМЕНТ HandlerMappingIntrospector, как и в прошлый раз !!!
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAt(basicAuthenticationFilter(http), org.springframework.security.web.authentication.www.BasicAuthenticationFilter.class)

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Настройки авторизации: ИСПОЛЬЗУЕМ ЯВНЫЙ AntPathRequestMatcher
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS
                        .requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.name())).permitAll()

                        // Static and WS
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/index.html"),
                                new AntPathRequestMatcher("/ws/**")
                        ).permitAll()

                        // Music Bands: GET (Read)
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/**", HttpMethod.GET.name())).permitAll()

                        // Music Bands: POST
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands", HttpMethod.POST.name())).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        // Music Bands: PATCH
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/**", HttpMethod.PATCH.name())).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        // Music Bands: DELETE
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/**", HttpMethod.DELETE.name())).hasAuthority("ROLE_ADMIN")

                        // Import XML: ИЗМЕНЕНИЕ ЗДЕСЬ 🟢
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/import/xml", HttpMethod.POST.name())).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        // Import History (Authenticated)
                        .requestMatchers(new AntPathRequestMatcher("/api/import-history", HttpMethod.GET.name())).authenticated()

                        // Studios & Albums (User/Admin)
                        // Примечание: для RequestMatchers без HttpMethod по умолчанию используется любой метод (ALL)
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/studios/**"),
                                new AntPathRequestMatcher("/api/albums/**")
                        ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        // Fallback: All other requests
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}