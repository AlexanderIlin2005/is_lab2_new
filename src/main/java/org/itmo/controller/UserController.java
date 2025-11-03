package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.mapper.UserMapper;
import org.itmo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.itmo.service.UserService; // НОВЫЙ ИМПОРТ
import org.itmo.model.enums.UserRole; // НОВЫЙ ИМПОРТ
import org.springframework.security.access.prepost.PreAuthorize; // НОВЫЙ ИМПОРТ
import org.springframework.web.bind.annotation.*; // Обновлённый импорт для @PatchMapping

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService; // НОВОЕ ПОЛЕ

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = (User) userDetails;
        
        return ResponseEntity.ok(userMapper.toUserDto(user));
    }

    // НОВОЕ: Получить всех пользователей (Только ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // НОВОЕ: Смена роли (Только ADMIN)
    @PatchMapping("/{userId}/role")
    // Требуется роль ADMIN и включенная @EnableGlobalMethodSecurity(prePostEnabled = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateRole(@PathVariable Long userId, @RequestParam UserRole newRole) {
        User updatedUser = userService.updateRole(userId, newRole);
        return ResponseEntity.ok(userMapper.toUserDto(updatedUser));
    }

}