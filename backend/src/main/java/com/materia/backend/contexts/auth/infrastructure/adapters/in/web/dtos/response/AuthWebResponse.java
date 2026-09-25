package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;

import java.util.Set;
import java.util.UUID;

/**
 * 🔹 AUTH WEB RESPONSE
 * 
 * HTTP response body returned upon successful login, registration, or token refresh.
 * Contains both top-level tokens/metadata and a nested `user` object for direct frontend compatibility.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthWebResponse {

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

    public AuthWebResponse() {
        this.tokenType = "Bearer";
        this.status = UserStatus.ACTIVE;
    }

    public AuthWebResponse(String accessToken, String refreshToken, String tokenType, UUID userId, String email, Role role, Set<String> permissions) {
        this(accessToken, refreshToken, tokenType, userId, email, role, permissions, null, null, null, null, UserStatus.ACTIVE);
    }

    public AuthWebResponse(String accessToken, String refreshToken, String tokenType, UUID userId, String email, Role role, Set<String> permissions,
                           String firstName, String lastName, String fullName, String phone, UserStatus status) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.permissions = permissions;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.phone = phone;
        this.status = status != null ? status : UserStatus.ACTIVE;
    }

    /**
     * Nested user object structure matching frontend expectation: response.data.user
     */
    public UserWebDto getUser() {
        String displayName = fullName != null && !fullName.isBlank() ? fullName :
                ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        if (displayName.isEmpty() && email != null) {
            displayName = email.split("@")[0];
        }

        return new UserWebDto(
                userId != null ? userId.toString() : null,
                displayName,
                email,
                role != null ? role.getCode() : "PURCHASER",
                firstName,
                lastName,
                phone,
                status != null ? status.getCode() : "ACTIVE"
        );
    }

    public record UserWebDto(
            String id,
            String name,
            String email,
            String role,
            String firstName,
            String lastName,
            String phone,
            String status
    ) {}

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
