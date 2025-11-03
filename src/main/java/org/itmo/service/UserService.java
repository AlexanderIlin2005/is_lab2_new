package org.itmo.service;

import org.itmo.model.User;
import org.itmo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.itmo.dto.RegistrationRequestDto; // НОВЫЙ ИМПОРТ
import org.itmo.model.enums.UserRole; // НОВЫЙ ИМПОРТ
import org.springframework.web.server.ResponseStatusException; // НОВЫЙ ИМПОРТ
import org.springframework.http.HttpStatus; // НОВЫЙ ИМПОРТ

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; 

    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        log.info("Attempting to load user by username: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: '{}'", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        
    
        

        return user;
    }

    
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    // НОВОЕ: Регистрация пользователя (по умолчанию USER)
    @Transactional
    public User registerNewUser(RegistrationRequestDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким именем уже существует.");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        // Хэширование пароля обязательно!
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(UserRole.USER); // <--- РОЛЬ ПО УМОЛЧАНИЮ

        log.info("Регистрация нового пользователя: '{}'", user.getUsername());
        return userRepository.save(user);
    }

    // НОВОЕ: Смена роли (для Admin)
    @Transactional
    public User updateRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        user.setRole(newRole);
        log.info("Роль пользователя '{}' изменена на: {}", user.getUsername(), newRole);
        return userRepository.save(user);
    }

    // НОВОЕ: Получить всех пользователей (для Admin)
    public List<User> findAll() {
        return userRepository.findAll();
    }

}