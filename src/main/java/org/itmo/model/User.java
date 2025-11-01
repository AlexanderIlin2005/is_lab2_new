package org.itmo.model;

import org.itmo.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority; // НОВЫЙ ИМПОРТ
import org.springframework.security.core.authority.SimpleGrantedAuthority; // НОВЫЙ ИМПОРТ
import org.springframework.security.core.userdetails.UserDetails; // НОВЫЙ ИМПОРТ

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
// Реализуем UserDetails
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // @Enumerated(EnumType.STRING) // <-- УДАЛИТЬ ЭТУ СТРОКУ
    @Column(nullable = false, columnDefinition = "user_role")
    private UserRole role;

    // --- Методы UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Роли в Spring Security должны иметь префикс "ROLE_"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash; // Возвращаем хэш
    }

    // В Spring Security username - это уникальный идентификатор
    @Override
    public String getUsername() {
        return username;
    }

    // Для простоты включаем все по умолчанию
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
    // -------------------------
}