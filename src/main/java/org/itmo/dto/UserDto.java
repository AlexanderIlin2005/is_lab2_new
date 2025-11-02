package org.itmo.dto;

import org.itmo.model.enums.UserRole;
import lombok.Value;

/**
 * DTO для передачи информации о текущем пользователе на фронтенд.
 */
@Value
public class UserDto {
    String username;
    UserRole role; // Фронтенд использует это поле для авторизации

    // !!! ДОБАВИТЬ ЭТИ ЯВНЫЕ ГЕТТЕРЫ !!!
    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}