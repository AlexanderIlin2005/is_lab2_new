package org.itmo.controller;

import org.itmo.dto.RegistrationRequestDto;
import org.itmo.dto.UserDto;
import org.itmo.mapper.UserMapper;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegistrationRequestDto registrationDto) {
        try {
            UserDto createdUser = userMapper.toUserDto(userService.registerNewUser(registrationDto));
            // 201 Created
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            // Обработка 409 Conflict из сервиса
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}