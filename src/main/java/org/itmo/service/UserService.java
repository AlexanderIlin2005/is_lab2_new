package org.itmo.service;

import org.itmo.model.User;
import org.itmo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Сервис, реализующий UserDetailsService для Spring Security.
 * Отвечает за загрузку данных пользователя по его имени (username).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по имени.
     * Этот метод вызывается Spring Security при попытке аутентификации.
     *
     * @param username Имя пользователя, полученное при аутентификации.
     * @return Объект UserDetails (в нашем случае, наша сущность User,
     * которая реализует UserDetails).
     * @throws UsernameNotFoundException Если пользователь не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Мы ищем нашу сущность User, которая реализует интерфейс UserDetails
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Метод для тестового создания пользователей при старте приложения (необязательно, но полезно).
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}