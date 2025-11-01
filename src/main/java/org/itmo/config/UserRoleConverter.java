package org.itmo.config;

import org.itmo.model.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

/**
 * Преобразует ENUM UserRole в его строковое имя и обратно.
 * Это необходимо для EclipseLink, чтобы он знал, как работать
 * с PostgreSQL ENUM-типами.
 */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        if (role == null) {
            return null;
        }
        // Преобразуем ENUM в его строковое имя (например, "ADMIN")
        return role.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Преобразуем строковое имя обратно в ENUM
        return Stream.of(UserRole.values())
                .filter(c -> c.name().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}