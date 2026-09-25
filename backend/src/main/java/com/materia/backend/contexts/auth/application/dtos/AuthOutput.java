package com.materia.backend.contexts.auth.application.dtos;

import com.materia.backend.common.application.BaseOutput;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;

import java.util.Set;
import java.util.UUID;

/**
 * 🔹 AUTH OUTPUT DTO
 * 
 * Application response containing JWT tokens and authenticated user metadata (email-based).
 */
public class AuthOutput extends BaseOutput {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UUID userId;
    private String email;
    private Role role;
    private Set<String> permissions;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private UserStatus status;

    public AuthOutput() {
        super();
        this.tokenType = "Bearer";
    }

    public AuthOutput(String accessToken, String refreshToken, UUID userId, String email, Role role, Set<String> permissions) {
        this(accessToken, refreshToken, userId, email, role, permissions, null, null, null, null, UserStatus.ACTIVE);
    }

    public AuthOutput(String accessToken, String refreshToken, UUID userId, String email, Role role, Set<String> permissions,
                      String firstName, String lastName, String fullName, String phone, UserStatus status) {
        super();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.permissions = permissions;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.phone = phone;
        this.status = status;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
