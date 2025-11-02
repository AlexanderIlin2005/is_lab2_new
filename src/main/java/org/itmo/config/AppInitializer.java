package org.itmo.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;


public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {


    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
                AppConfig.class,
                SecurityConfig.class,       // <-- ФИКС: Явная регистрация Security
                PasswordEncoderConfig.class // <-- ФИКС: Явная регистрация Encoder
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }


    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    // !!! НЕТ МЕТОДА onStartup(ServletContext) ЗДЕСЬ !!!
}