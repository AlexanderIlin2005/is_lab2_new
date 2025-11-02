package org.itmo.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("org.itmo")
@EnableJpaRepositories(basePackages = "org.itmo.repository")
@EnableTransactionManagement
@Import({SecurityConfig.class, PasswordEncoderConfig.class, JacksonConfig.class})
public class AppConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setPersistenceUnitName("my-persistence-unit");
        em.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());

        // 💡 КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ:
        // Принудительно указываем EclipseLink сканировать весь пакет org.itmo
        // для поиска ВСЕХ сущностей (@Entity) и конвертеров (@Convert).
        em.setPackagesToScan("org.itmo");

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}