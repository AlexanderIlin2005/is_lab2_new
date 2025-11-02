package org.itmo.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // Явно перечисляем все корневые конфигурации. Это самый надежный способ.
        return new Class[]{AppConfig.class, SecurityConfig.class, PasswordEncoderConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        // Диспетчер будет обрабатывать все URL
        return new String[]{"/"};
    }

    // Метод onStartup удален
}