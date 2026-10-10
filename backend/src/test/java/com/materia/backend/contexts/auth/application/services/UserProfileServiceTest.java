package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.application.dtos.UserProfileOutput;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.events.UserProfileUpdatedEvent;
import com.materia.backend.contexts.auth.domain.exceptions.UserNotFoundException;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserRepository users;
    @Mock private ApplicationEventPublisher events;

    private User user(UUID id) {
        return User.builder().id(id).email("ada@materia.test").passwordHash("h").role(Role.PURCHASER)
                .firstName("Old").lastName("Name").fullName("Old Name").phone("000").build();
    }

    @Test
    @DisplayName("profile: a save trims the fields, recomputes the full name and announces the change")
    void update_savesAndPublishes() {
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(user(id)));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileOutput out = new UserProfileService(users, events)
                .updateProfile(id, new UpdateProfileInput("  Ada ", " Lovelace ", "  "));

        assertEquals("Ada Lovelace", out.fullName());
        assertNull(out.phone(), "a blank phone is cleared");
        ArgumentCaptor<UserProfileUpdatedEvent> event = ArgumentCaptor.forClass(UserProfileUpdatedEvent.class);
        verify(events).publishEvent(event.capture());
        assertEquals(id, event.getValue().getUserId());
        assertEquals("Ada", event.getValue().getFirstName());
        assertEquals("Lovelace", event.getValue().getLastName());
    }

    @Test
    @DisplayName("profile: an unknown account is not found and nothing is announced")
    void update_unknownUser_isNotFound() {
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> new UserProfileService(users, events)
                .updateProfile(id, new UpdateProfileInput("A", "B", null)));
        verifyNoInteractions(events);
    }

    @Test
    @DisplayName("profile: missing or blank names clear the full name rather than keeping the old one")
    void update_blankNames_clearTheFullName() {
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(user(id)));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserProfileService service = new UserProfileService(users, events);

        assertNull(service.updateProfile(id, new UpdateProfileInput(null, "  ", null)).fullName());
        assertEquals("Ada", service.updateProfile(id, new UpdateProfileInput("Ada", null, null)).fullName());
        assertEquals("Lovelace", service.updateProfile(id, new UpdateProfileInput(" ", "Lovelace", "0600")).fullName());
    }
}
