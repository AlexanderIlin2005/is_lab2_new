package org.itmo.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Принудительно запускает механизм регистрации Spring Security Filter Chain
 * на ServletContext при старте. Это необходимо в embedded-окружениях
 * (Jetty + Fat JAR), где авто-регистрация может сбоить.
 */
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {

    // Указываем наш основной класс конфигурации безопасности
    public SecurityInitializer() {
        super(SecurityConfig.class);
    }
}