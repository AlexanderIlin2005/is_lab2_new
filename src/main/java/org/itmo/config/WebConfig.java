package org.itmo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

// ДОБАВИТЬ НОВЫЕ ИМПОРТЫ
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List; // ВАЖНО: убедитесь, что List импортирован

// ДОБАВИТЬ НОВЫЕ ИМПОРТЫ
import com.fasterxml.jackson.databind.ObjectMapper;

// НОВЫЙ ИМПОРТ
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

// НОВЫЙ ИМПОРТ
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport // <-- ДОБАВИТЬ ЭТУ АННОТАЦИЮ
@ComponentScan("org.itmo")
public class WebConfig implements WebMvcConfigurer {

    private final ApplicationContext applicationContext;

    private final SecurityConfig securityConfig; // <-- КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ: Добавить поле

    @Autowired
    public WebConfig(ApplicationContext applicationContext, SecurityConfig securityConfig) {
        this.applicationContext = applicationContext;
        this.securityConfig = securityConfig; // <-- Сохранить
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);

        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        registry.viewResolver(resolver);
    }

    /**
     * !!! ФИНАЛЬНОЕ ИСПРАВЛЕНИЕ: Отключаем поиск файлов по суффиксу. !!!
     * Это гарантирует, что запросы /api/** не будут перехвачены Thymeleaf,
     * который ошибочно ищет шаблон, а будут обрабатываться RestController'ом.
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Отключаем поиск шаблонов по суффиксу (например, .xml, .json),
        // что предотвращает конфликт с ViewResolver.
        configurer.setUseSuffixPatternMatch(false);

        // Также полезно отключить совпадение с конечным слешем для чистоты API
        configurer.setUseTrailingSlashMatch(false);
    }

    /**
     * !!! ЗАМЕНА !!! Переопределяем ВСЕ конвертеры и добавляем только наш Jackson.
     * Это гарантирует, что Jackson получит приоритет и будет использован.
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Создаем ObjectMapper и регистрируем модуль для ZonedDateTime (важно!)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Это опционально, но полезно для корректной работы с модулями
        objectMapper.findAndRegisterModules();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        // Очищаем и добавляем только наш конвертер, чтобы избежать конфликтов
        converters.clear(); // <-- Важно! Очищаем список.
        converters.add(converter);
    }

    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
     */
}