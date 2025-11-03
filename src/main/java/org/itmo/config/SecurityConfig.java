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
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;








import org.springframework.security.web.util.matcher.AntPathRequestMatcher; 

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement(proxyTargetClass = true)
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder; 

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

    
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        authProvider.setUserDetailsService(userService);
        
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    
    @Bean
    public BasicAuthenticationFilter basicAuthenticationFilter(HttpSecurity http) throws Exception {
        
        AuthenticationManager authenticationManager = authenticationManager();
        return new BasicAuthenticationFilter(authenticationManager, authenticationEntryPoint());
    }


    
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

                
                .authorizeHttpRequests(auth -> auth
                        
                        .requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.name())).permitAll()

                        
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/index.html"),
                                new AntPathRequestMatcher("/ws/**")
                        ).permitAll()

                        
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/**", HttpMethod.GET.name())).permitAll()

                        
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands", HttpMethod.POST.name())).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/**", HttpMethod.PATCH.name())).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/**", HttpMethod.DELETE.name())).hasAuthority("ROLE_ADMIN")

                        
                        .requestMatchers(new AntPathRequestMatcher("/api/music-bands/import/xml", HttpMethod.POST.name())).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        
                        .requestMatchers(new AntPathRequestMatcher("/api/import-history", HttpMethod.GET.name())).authenticated()

                        
                        
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/studios/**"),
                                new AntPathRequestMatcher("/api/albums/**")
                        ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}