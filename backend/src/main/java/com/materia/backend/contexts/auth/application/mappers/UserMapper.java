package com.materia.backend.contexts.auth.application.mappers;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.domain.entities.User;
import org.springframework.stereotype.Component;

/**
 * 🔹 USER APPLICATION MAPPER
 * 
 * Maps between application DTOs and domain User entity (email-only).
 */
@Component
public class UserMapper {

    public AuthOutput toAuthOutput(User user, String accessToken, String refreshToken) {
        if (user == null) return null;

        return new AuthOutput(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getRole() != null ? user.getRole().getPermissions() : null,
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus()
        );
    }
}
