package org.itmo.mapper;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    /**
     * Преобразует сущность User в DTO.
     */
    UserDto toUserDto(User user);
}