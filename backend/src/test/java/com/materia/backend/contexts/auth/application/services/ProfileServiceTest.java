package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.ProfileOutput;
import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.exceptions.UserNotFoundException;
import com.materia.backend.contexts.auth.domain.ports.out.EmployeeProfileSync;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The signed-in user's own profile: what is shown, what may be changed, and the employee record kept in step. */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeProfileSync employeeProfileSync;

    private ProfileService service;
    private final UUID userId = UUID.randomUUID();
    private User user;

    @BeforeEach
    void setUp() {
        service = new ProfileService(userRepository);
        service.setEmployeeProfileSync(employeeProfileSync);
        user = User.builder()
                .id(userId)
                .email("sara@materia.ma")
                .firstName("Sara")
                .lastName("Alami")
                .phone("+212 600 000 000")
                .department("Procurement")
                .role(Role.PURCHASER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("rule: the profile shows the account, its role and the role's permissions")
    void getProfile_returnsAccount() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ProfileOutput profile = service.getProfile(userId);

        assertThat(profile.email()).isEqualTo("sara@materia.ma");
        assertThat(profile.fullName()).isEqualTo("Sara Alami");
        assertThat(profile.department()).isEqualTo("Procurement");
        assertThat(profile.role()).isEqualTo("PURCHASER");
        assertThat(profile.status()).isEqualTo("ACTIVE");
        assertThat(profile.permissions()).isNotEmpty().isSorted();
    }

    @Test
    @DisplayName("rule: an unknown account is reported as not found")
    void getProfile_unknownUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(userId)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("rule: users change their name and phone; the full name follows and the employee record is updated")
    void updateProfile_changesNameAndPhone() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileOutput profile = service.updateProfile(userId, new UpdateProfileInput("  Salma ", "Bennani", " 0522 11 22 33 "));

        assertThat(profile.firstName()).isEqualTo("Salma");
        assertThat(profile.lastName()).isEqualTo("Bennani");
        assertThat(profile.fullName()).isEqualTo("Salma Bennani");
        assertThat(profile.phone()).isEqualTo("0522 11 22 33");
        assertThat(profile.email()).isEqualTo("sara@materia.ma");
        assertThat(profile.role()).isEqualTo("PURCHASER");
        verify(employeeProfileSync).syncContactDetails(userId, "Salma", "Bennani", "0522 11 22 33");
    }

    @Test
    @DisplayName("rule: an empty phone clears it")
    void updateProfile_blankPhoneClears() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileOutput profile = service.updateProfile(userId, new UpdateProfileInput("Sara", "Alami", "  "));

        assertThat(profile.phone()).isNull();
        verify(employeeProfileSync).syncContactDetails(userId, "Sara", "Alami", null);
    }

    @Test
    @DisplayName("rule: first and last name are required, and the phone only takes phone characters")
    void updateProfile_validates() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.updateProfile(userId, new UpdateProfileInput(" ", "Alami", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("First name");
        assertThatThrownBy(() -> service.updateProfile(userId, new UpdateProfileInput("Sara", null, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Last name");
        assertThatThrownBy(() -> service.updateProfile(userId, new UpdateProfileInput("Sara", "Alami", "call me")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Phone");
        assertThatThrownBy(() -> service.updateProfile(userId, new UpdateProfileInput("x".repeat(101), "Alami", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("100");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(employeeProfileSync);
    }

    @Test
    @DisplayName("rule: without an employee directory the profile is still saved")
    void updateProfile_withoutSync() {
        ProfileService standalone = new ProfileService(userRepository);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(standalone.updateProfile(userId, new UpdateProfileInput("Sara", "Idrissi", null)).fullName())
                .isEqualTo("Sara Idrissi");
    }
}
