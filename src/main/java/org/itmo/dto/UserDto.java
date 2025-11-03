package org.itmo.dto;

import org.itmo.model.enums.UserRole;
import lombok.Value;


@Value
public class UserDto {
    private Long id; // <-- ЭТО ПОЛЕ ДОЛЖНО БЫТЬ ОБЯЗАТЕЛЬНО
    String username;
    UserRole role;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}