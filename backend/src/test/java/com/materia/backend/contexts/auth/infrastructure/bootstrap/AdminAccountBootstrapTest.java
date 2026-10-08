package com.materia.backend.contexts.auth.infrastructure.bootstrap;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The first administrator of a fresh install: created once, from configuration, only when no account exists. */
@ExtendWith(MockitoExtension.class)
class AdminAccountBootstrapTest {

    @Mock private UserRepository users;
    @Mock private PasswordEncoder encoder;

    private AdminAccountBootstrap bootstrap(String email, String password) {
        return new AdminAccountBootstrap(users, encoder, email, password);
    }

    @Test
    @DisplayName("rule: with no account, an administrator is created who must change the password at first sign-in")
    void emptyDatabase_createsAdmin() {
        when(users.count()).thenReturn(0L);
        when(encoder.encode("Start1!pass")).thenReturn("hashed");

        assertThat(bootstrap(" Admin@Materia.ma ", "Start1!pass").createIfNoAccount()).isTrue();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("admin@materia.ma");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().isMustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("rule: once any account exists nothing is created, even with the variables still set")
    void existingAccounts_noChange() {
        when(users.count()).thenReturn(3L);

        assertThat(bootstrap("admin@materia.ma", "Start1!pass").createIfNoAccount()).isFalse();

        verify(users, never()).save(any());
    }

    @Test
    @DisplayName("rule: missing variables or a password failing the policy create nothing")
    void missingOrWeak_noAccount() {
        when(users.count()).thenReturn(0L);

        assertThat(bootstrap("", "Start1!pass").createIfNoAccount()).isFalse();
        assertThat(bootstrap("admin@materia.ma", "").createIfNoAccount()).isFalse();
        assertThat(bootstrap("admin@materia.ma", "password").createIfNoAccount()).isFalse();

        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }
}
