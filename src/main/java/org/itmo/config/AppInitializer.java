package org.itmo.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.filter.DelegatingFilterProxy; // <-- НОВЫЙ ИМПОРТ
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.FilterRegistration; // <-- НОВЫЙ ИМПОРТ
import java.util.EnumSet;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // AppConfig должен импортировать SecurityConfig и т.д.
        return new Class[]{AppConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    // !!! КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ: ПРИНУДИТЕЛЬНАЯ РЕГИСТРАЦИЯ ФИЛЬТРА SECURITY !!!
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // Сначала запускаем стандартный процесс инициализации (Root и Servlet контексты)
        super.onStartup(servletContext);

        // Ручная регистрация фильтра Spring Security
        FilterRegistration.Dynamic securityFilter = servletContext.addFilter(
                "springSecurityFilterChain", // Имя бина Spring Security
                new DelegatingFilterProxy("springSecurityFilterChain") // Фильтр-делегат
        );

        // Регистрируем его для ВСЕХ URL ("/*") и делаем его ПЕРВЫМ
        // EnumSet.of(DispatcherType.REQUEST) гарантирует, что он будет выполняться только для запросов
        securityFilter.addMappingForUrlPatterns(
                EnumSet.of(jakarta.servlet.DispatcherType.REQUEST),
                false,
                "/*"
        );
    }
    // -----------------------------------------------------------------------
}