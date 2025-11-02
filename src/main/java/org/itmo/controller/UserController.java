package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.mapper.UserMapper;
import org.itmo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // <-- НОВЫЙ ИМПОРТ
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        // !!! ГЛАВНОЕ: ПРОВЕРКА НА NULL !!!
        if (userDetails == null) {
            // Если пользователь не аутентифицирован (получен 401), возвращаем 401
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = (User) userDetails;
        // Используем DTO и Mapper
        return ResponseEntity.ok(userMapper.toUserDto(user));
    }
}