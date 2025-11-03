package org.itmo.dto;

import org.itmo.model.enums.UserRole;
import lombok.Value;


@Value
public class UserDto {
    String username;
    UserRole role; 

    
    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}